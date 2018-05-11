package forex.interfaces.api.rates

import java.time.OffsetDateTime

import forex.domain._
import io.circe._
import io.circe.generic.semiauto._

object Protocol {

  final case class GetApiRequest(
      from: Currency,
      to: Currency
  )

  final case class GetApiResponse(
      from: Currency,
      to: Currency,
      price: Price,
      timestamp: Timestamp
  )

  object GetApiResponse {
    implicit val encoder: Encoder[GetApiResponse] = deriveEncoder[GetApiResponse]
    implicit val decoder: Decoder[GetApiResponse] = deriveDecoder[GetApiResponse]
  }

  final case class ConvertApiRequest(
      from: Currency,
      to: Currency,
      quantity: Quantity
  )

  final case class ConvertApiResponse(
      value: Price,
      text: Text,
      timestamp: Timestamp
  )

  object ConvertApiResponse {
    implicit val convertEncoder: Encoder[ConvertApiResponse] = deriveEncoder
    implicit val convertDecoder: Decoder[ConvertApiResponse] = deriveDecoder
  }

}
