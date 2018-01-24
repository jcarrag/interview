package users

import users.config._
import users.main._

object Main extends App {

  val config = ApplicationConfig(
    executors = ExecutorsConfig(
      services = ExecutorsConfig.ServicesConfig(
        parallellism = 4
      )
    ),
    services = ServicesConfig(
      users = ServicesConfig.UsersConfig(
        failureProbability = 0.1,
        timeoutProbability = 0.1
      ),
      http = ServicesConfig.HttpConfig(
        host = "localhost",
        port = 8080
      )
    )
  )

  val application = Application.fromApplicationConfig.run(config)
  val bindingFuture = application.runServer
  println("Running...")

  import application.services.ec
  sys.addShutdownHook {
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => application.system.terminate())
  }
}
