package forex.interfaces.api.utils

import akka.http.scaladsl._
import forex.processes._

object ApiExceptionHandler {

  def apply(): server.ExceptionHandler =
    server.ExceptionHandler {
      case re: RatesError ⇒
        ctx ⇒
          ctx.complete(s"Something went wrong in the rates process, possible cause is ${re.msg}")
      case e: Throwable ⇒
        ctx ⇒
          ctx.complete(s"Something else went wrong, ${e.getMessage}")
    }

}
