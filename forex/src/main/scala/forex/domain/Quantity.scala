package forex.domain

import akka.http.scaladsl.unmarshalling.Unmarshaller
import io.circe.Decoder
import io.circe.generic.extras.wrapped._

case class Quantity(value: BigDecimal) extends AnyVal {
  def applyConversion(price: Price): Price = Price(this.value * price.value)
}
object Quantity {
  implicit val quantityDecoder: Decoder[Quantity] = deriveUnwrappedDecoder
  implicit val quantityUnmarshaller =
    Unmarshaller.strict[String, Quantity](str => Quantity(BigDecimal(str)))
}
