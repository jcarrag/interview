package forex.interfaces.api.rates

import forex.domain._
import forex.processes.rates.messages._

object Converters {
  import Protocol._

  def toGetRequest(
      request: GetApiRequest
  ): GetRequest =
    GetRequest(
      from = request.from,
      to = request.to
    )

  def toGetApiResponse(
      rate: Rate
  ): GetApiResponse =
    GetApiResponse(
      from = rate.pair.from,
      to = rate.pair.to,
      price = rate.price,
      timestamp = rate.timestamp
    )

  def toConvertRequest(
      request: ConvertApiRequest
  ): ConvertRequest =
    ConvertRequest(
      from = request.from,
      to = request.to,
      quantity = request.quantity
    )

  def toConvertApiResponse(
    conversion: Conversion
  ): ConvertApiResponse =
    ConvertApiResponse(
      value = conversion.value,
      text = conversion.text,
      timestamp = conversion.timestamp
    )

}
