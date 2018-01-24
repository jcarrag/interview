package users.services.client

import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.parser.decode

import org.scalatest.{ Matchers, WordSpec }
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.fixture

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server._
import akka.http.scaladsl.model.headers.BasicHttpCredentials

import scala.collection.mutable.{Map => MMap}

import users.config._
import users.main.Application
import users.services.client.Http._

class RouteSpec extends fixture.WordSpec with Matchers with ScalatestRouteTest {

  val config: ApplicationConfig = ApplicationConfig(
    executors = ExecutorsConfig(
      services = ExecutorsConfig.ServicesConfig(
        parallellism = 4
      )
    ),
    services = ServicesConfig(
      users = ServicesConfig.UsersConfig(
        failureProbability = 0.0,
        timeoutProbability = 0.0
      ),
      http = ServicesConfig.HttpConfig(
        host = "localhost",
        port = 8080
      )
    )
  )

  val application = Application.fromApplicationConfig.run(config)

  val route = application.services.route

  val password = "password"->"password"
  val user = Map[String, String](
    "userName"->"Foo",
    "emailAddress"->"foo@email.com"
  )


  val adminCred = BasicHttpCredentials("admin", "admin")
  def userCred(id: String) = BasicHttpCredentials(id, "")

  def toRequest[A : Encoder](ent: A) = HttpEntity(`application/json`, ent.asJson.noSpaces)
  def toMap(ent: String): Map[String, String] = decode[Map[String, String]](ent).right.get
  def id(f: FixtureParam): String = f.m.get("id").get

  val store: MMap[String, String] = MMap.empty

  case class FixtureParam(m: MMap[String, String])
  override def withFixture(test: OneArgTest) = {
    val fix = FixtureParam(store)
    withFixture(test.toNoArgTest(fix))
  }
  "The service" should {

    "as a user: be able to insert a new user" in { f =>
      Post("/v1/register", toRequest(user + password)) ~> route ~> check {
        status shouldEqual StatusCodes.OK
        contentType shouldEqual `application/json`
        f.m += ("id"->toMap(responseAs[String]).get("id").get)
        toMap(responseAs[String]) shouldEqual user + ("id"->id(f))
      }
    }

    "as user: not be able to reinsert the same user" in { f =>
      Post("/v1/register", toRequest(user)) ~> route ~> check {
        status shouldEqual StatusCodes.BadRequest
        contentType shouldEqual `application/json`
        responseAs[String] shouldEqual """{"message":"User exists","description":"Cannot overwrite given user"}"""
      }
    }

    "as a user: get all users" in { f =>
      Get("/v1/user") ~>
        addCredentials(userCred(id(f))) ~>
        route ~>
        check {
          status shouldEqual StatusCodes.OK
          contentType shouldEqual `application/json`
          decode[List[String Map String]](responseAs[String]).right.get shouldEqual List(user + ("id"->id(f)))
        }
    }

    "as a user: be able to get the created user" in { f =>
      Get(s"/v1/user/${id(f)}") ~>
        addCredentials(userCred(id(f))) ~>
        route ~>
        check {
          status shouldEqual StatusCodes.OK
          contentType shouldEqual `application/json`
          toMap(responseAs[String]) shouldEqual user + ("id"->id(f))
        }
    }

    val newEmail = "emailAddress"->"foo@email.com"
    "as that user: be able to update the created user's email address" in { f =>
      Put(s"/v1/user/${id(f)}/update/email", toRequest(Map(newEmail))) ~>
        addCredentials(userCred(id(f))) ~>
        route ~>
        check {
          status shouldEqual StatusCodes.OK
          contentType shouldEqual `application/json`
          toMap(responseAs[String]) shouldEqual user + newEmail + ("id"->id(f))
        }
    }

    val newPass = "password"->"foo"
    "as that user: be able to update the created user's password" in { f =>
      Put(s"/v1/user/${id(f)}/update/password", toRequest(Map(newPass))) ~>
        addCredentials(userCred(id(f))) ~>
        route ~>
        check {
          status shouldEqual StatusCodes.OK
          contentType shouldEqual `application/json`
          toMap(responseAs[String]) shouldEqual user + newEmail + ("id"->id(f))
        }
    }

    "as an admin: be able to reset a user's password" in { f =>
      Put(s"/v1/admin/user/${id(f)}/reset") ~>
        addCredentials(adminCred) ~>
        route ~>
        check {
          status shouldEqual StatusCodes.OK
        }
    }

    "as an admin: be able to block a user" in { f =>
      Put(s"/v1/admin/user/${id(f)}/block") ~>
        addCredentials(adminCred) ~>
        route ~>
        check {
          status shouldEqual StatusCodes.OK
        }
    }

    "as an admin: be unable to block a blocked user" in { f =>
      Put(s"/v1/admin/user/${id(f)}/block") ~>
        addCredentials(adminCred) ~>
        route ~>
        check {
          status shouldEqual StatusCodes.BadRequest
          responseAs[String] shouldEqual """{"message":"User is blocked","description":"Cannot block a blocked user"}"""
        }
    }

    "as an admin: be able to unblock a blocked user" in { f =>
      Put(s"/v1/admin/user/${id(f)}/unblock") ~>
        addCredentials(adminCred) ~>
        route ~>
        check {
          status shouldEqual StatusCodes.OK
        }
    }

    "as an admin: be unable to delete an unblocked user" in { f =>
      Delete(s"/v1/admin/user/${id(f)}/delete") ~>
        addCredentials(adminCred) ~>
        route ~>
        check {
          status shouldEqual StatusCodes.BadRequest
        }
    }

    "as an admin: be able to reblock an unblocked user" in { f =>
      Put(s"/v1/admin/user/${id(f)}/block") ~> addCredentials(adminCred) ~> route
    }

    "as an admin: be able to delete a blocked user" in { f =>
      Delete(s"/v1/admin/user/${id(f)}/delete") ~>
        addCredentials(adminCred) ~>
        route ~>
        check {
          status shouldEqual StatusCodes.OK
        }
    }
  }
}
