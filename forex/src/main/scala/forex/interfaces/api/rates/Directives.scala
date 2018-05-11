package forex.interfaces.api.rates

import akka.http.scaladsl._
import forex.domain._

trait Directives {
  import server.Directives._
  import unmarshalling.Unmarshaller
  import Protocol._

  def getApiRequest: server.Directive1[GetApiRequest] =
    for {
      from ← parameter('from.as[Currency])
      to ← parameter('to.as[Currency])
    } yield GetApiRequest(from, to)

  def convertApiRequest: server.Directive1[ConvertApiRequest] =
    for {
      from <- parameter('from.as[Currency])
      to   <- parameter('to.as[Currency])
      quantity <- parameter('quantity.as[Quantity])
    } yield ConvertApiRequest(from, to, quantity)
}

object Directives extends Directives
