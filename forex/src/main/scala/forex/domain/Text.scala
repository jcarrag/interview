package forex.domain

import cats.syntax.show._
import io.circe._
import io.circe.generic.extras.wrapped._

case class Text(value: String) extends AnyVal

object Text{
  def describe(
      quantity: Quantity,
      from: Currency,
      value: Price,
      to: Currency
  ): Text =
    Text(s"${quantity.value} ${from.show} is worth ${value.value} ${to.show}")

  implicit val textEncoder: Encoder[Text] = deriveUnwrappedEncoder
  implicit val textDecoder: Decoder[Text] = deriveUnwrappedDecoder
}
