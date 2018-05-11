package forex.services.oneforge

import cats.instances.all._
import cats.syntax.apply._
import cats.syntax.either._

import java.time.{OffsetDateTime , Instant, ZoneId}

import forex.domain.{Currency, Price, Timestamp}
import io.circe._
import io.circe.generic.semiauto._
import io.circe.generic.extras.wrapped._

object Responses {
  case class Bid(value: BigDecimal) extends AnyVal

  object Bid {
    implicit val decoder: Decoder[Bid] = deriveUnwrappedDecoder[Bid]
  }

  case class Ask(value: BigDecimal) extends AnyVal

  object Ask {
    implicit val decoder: Decoder[Ask] = deriveUnwrappedDecoder[Ask]
  }

  implicit val offsetDTEpochDecoder: Decoder[OffsetDateTime] =
    Decoder.decodeLong.emap { epoch =>
      Either.catchNonFatal(
        OffsetDateTime.ofInstant(
          Instant.ofEpochSecond(epoch),
          ZoneId.systemDefault()
        )
      ).leftMap(t => s"failed to decode $epoch to OffsetDateTime, with error: ${t.getMessage}")
    }

  implicit val decoderTs: Decoder[Timestamp] =
    deriveUnwrappedDecoder[Timestamp]

  implicit val currencyPairDecoder: Decoder[(Currency, Currency)] =
    Decoder.decodeString.emap { symbol =>
      Either.catchNonFatal{
        if (symbol.length != 6) throw new IllegalArgumentException(s"failed to decode $symbol must have length 6")
        else (Currency.fromString(symbol.take(3)), Currency.fromString(symbol.takeRight(3)))
      }.leftMap(t => s"failed to decode $symbol to (Currency, Currency), with error: ${t.getMessage}")
    }

  final case class QuoteResponse(
    symbol: (Currency, Currency),
    bid: Bid,
    ask: Ask,
    price: Price,
    timestamp: Timestamp
  )

  object QuoteResponse {
    implicit val decodeQuoteResponse: Decoder[QuoteResponse] =
      Decoder.instance[QuoteResponse]{ c =>
        for {
          symbol <- c.downField("symbol").as[(Currency, Currency)]
          bid    <- c.downField("bid").as[Bid]
          ask    <- c.downField(("ask")).as[Ask]
          price  <- c.downField("price").as[Price]
          ts     <- c.downField("timestamp").as[Timestamp]
        } yield QuoteResponse(symbol, bid, ask, price, ts)
      }
  }
}