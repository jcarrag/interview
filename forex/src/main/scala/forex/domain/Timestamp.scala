package forex.domain

import io.circe._
import io.circe.generic.extras.wrapped._
import io.circe.java8.time._
import java.time.OffsetDateTime

case class Timestamp(value: OffsetDateTime) extends AnyVal

object Timestamp {
  def now: Timestamp =
    Timestamp(OffsetDateTime.now)

  implicit val encoderTs: Encoder[Timestamp] =
    deriveUnwrappedEncoder[Timestamp]

  implicit val decoderTs: Decoder[Timestamp] =
    deriveUnwrappedDecoder[Timestamp]
}
