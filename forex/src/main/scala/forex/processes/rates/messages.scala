package forex.processes.rates

import forex.domain._
import scala.util.control.NoStackTrace

package messages {
  sealed trait Error extends Throwable with NoStackTrace {
    val msg: String
  }
  object Error {
    final case class Custom(msg: String) extends Error
    final case object Generic extends Error {
      val msg = "generic error"
    }
    final case class System(underlying: Throwable) extends Error {
      val msg = s"${underlying.getMessage}"
    }
  }

  final case class GetRequest(
      from: Currency,
      to: Currency
  )

  final case class ConvertRequest(
      from: Currency,
      to: Currency,
      quantity: Quantity
  )
}
