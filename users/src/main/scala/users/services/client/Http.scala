package users.services.client

import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._

import cats.data.EitherT
import cats.implicits._

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.RouteResult._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.Credentials

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

import users.domain
import users.services.UserManagement
import users.services.usermanagement.Error
import users.services.client.ClientDomain._
import users.services.client.ClientDomain.Encoders._
import users.services.client.ClientDomain.Decoders._
import users.services.client.Adapter._
import users.services.client.CirceSupport._

object Http {

  object Entity {
    case class Registration(userName: UserName, emailAddress: EmailAddress, password: Option[Password])
    case class Email(emailAddress: EmailAddress)
    case class Pass(password: Password)
    case class Error(message: String, description: String)
  }

  def badEntity(msg: String, desc: String) = (BadRequest, Entity.Error(msg,desc))
  def errorToHttp(e: Error):(StatusCode, Entity.Error) = e match {
    case Error.Exists => badEntity("User exists", "Cannot overwrite given user")
    case Error.NotFound => badEntity("User not found", "Cannot find requested user")
    case Error.Active => badEntity("User is active", "Cannot delete or block an active user")
    case Error.Deleted => badEntity("User is deleted", "Cannot modify deleted user")
    case Error.Blocked => badEntity("User is blocked", "Cannot block a blocked user")
    case Error.System(ex) => (InternalServerError, Entity.Error("System error", ex.getMessage))
  }

  def transformPayload[D <: domain.Domain, C <: ClientDomain](f: D => C)(payload: Either[Error, D]) = payload match {
    case Left(e) => Left(errorToHttp(e))
    case Right(p) => Right(f(p))
  }

  val transformUser = transformPayload[domain.User, User](_.toClient)_

  val timeoutResponse = HttpResponse(
    GatewayTimeout,
    entity = HttpEntity(
      `application/json`,
      Entity.Error("Gateway Timeout", "Sorry, could not respond in a timely manner").asJson.noSpaces
    )
  )

  def route(userManagement: UserManagement[Future[?]])(implicit ec: ExecutionContext) = {

    def isUserAuthenticator(credentials: Credentials): Future[Option[String]] = credentials match {
      case Credentials.Provided(id) =>
        (for {
           user <- EitherT(userManagement.get(User.Id(id).toDomain))
         } yield user.id.toClient.value).value.map(_.toOption)
      case _ => Future.successful(None)
    }

    def isAdminAuthenticator(credentials: Credentials): Option[String] = credentials match {
      case p@Credentials.Provided(id) if (id == "admin" && p.verify("admin")) => Some(id)
      case _ => None
    }

    def isUserOrAdminAuthenticator(credentials: Credentials): Future[Option[String]] = credentials match {
      case p@Credentials.Provided(id) if (id == "admin" && p.verify("admin")) => Future.successful(Some(id))
      case Credentials.Provided(id) =>
        (for {
           user <- EitherT(userManagement.get(User.Id(id).toDomain))
         } yield user.id.toClient.value).value.map(_.toOption)
      case _ => Future.successful(None)
    }


    val withFailedFuture = mapRouteResultFuture(
      _.recover {
        case ex => Complete(HttpResponse(StatusCodes.InternalServerError))
      }
    )

    val noAuthRoute =
      path("register") {
        post {
          entity(as[Entity.Registration]) { case r =>
            complete {
              userManagement
                .signUp(
                  userName = r.userName.toDomain,
                  emailAddress = r.emailAddress.toDomain,
                  password = r.password.map(_.toDomain),
                  )
                .map(transformUser)
            }
          }
        }
      }

    val userOnlyRoute =
      authenticateBasicAsync(realm = "user domain", isUserAuthenticator) { validatedUserName =>
        pathPrefix("user" / Segment) { rawId =>
          val id = User.Id(rawId).toDomain
          put {
            pathPrefix("update") {
              path ("email") {
                entity(as[Entity.Email]) { e =>
                  complete {
                    userManagement
                      .updateEmail(
                        id,
                        e.emailAddress.toDomain
                      )
                      .map(transformUser)
                  }
                }
              } ~
                path ("password") {
                  entity(as[Entity.Pass]) { p =>
                    complete {
                      userManagement
                        .updatePassword(
                          id,
                          p.password.toDomain
                        )
                        .map(transformUser)
                    }
                  }
                }
            }
          }
        }
      }

    val adminOnlyRoute =
      authenticateBasic(realm = "admin domain", isAdminAuthenticator) { validatedAdminName =>
        pathPrefix("admin" / "user" / Segment) { rawId =>
          val id = User.Id(rawId).toDomain
          path("reset") {
            complete {
              userManagement
                .resetPassword(id)
                .map(transformUser)
            }
          } ~
            path("block") {
              complete {
                userManagement
                  .block(id)
                  .map(transformUser)
              }
            } ~
            path("unblock") {
              complete {
                userManagement
                  .unblock(id)
                  .map(transformUser)
              }
            } ~
            delete {
              complete {
                userManagement
                  .delete(id)
                  .map(transformPayload(ClientAdapter[domain.Done, Done].toClient))
              }
            }
        }
      }

    val userAndAdminRoute =
      authenticateBasicAsync(realm = "user and admin domain", isUserOrAdminAuthenticator) { validatedUserName =>
        path("user") {
          get {
            complete{
              userManagement
                .all()
                .map(_.map(_.map(_.toClient)))
            }
          }
        } ~
          path("user" / Segment) { rawId =>
            val id = User.Id(rawId).toDomain
            pathEndOrSingleSlash{
              get {
                complete {
                  userManagement
                    .get(id)
                    .map(transformUser)
                }
              }
            }
          }
      }

    val compositeRoute = noAuthRoute ~ userOnlyRoute ~ adminOnlyRoute ~ userAndAdminRoute

    val versionedRoute =
      pathPrefix("v1") {
        compositeRoute
      }

    withFailedFuture {
      withRequestTimeout(1.seconds, _ => timeoutResponse) {
        versionedRoute
      }
    }
  }
}
