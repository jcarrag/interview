package users.main

import scala.concurrent.Future
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import cats.data._
import users.config._

object Application {
  val reader: Reader[Services, Application] =
    Reader(Application.apply)

  val fromApplicationConfig: Reader[ApplicationConfig, Application] =
    Services.fromApplicationConfig andThen reader
}

case class Application(
    services: Services
) {
  import services._

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()

  val ServicesConfig.HttpConfig(host, port) = config.http
  def runServer():Future[Http.ServerBinding] = Http().bindAndHandle(route, host, port)
}
