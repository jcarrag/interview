package forex.domain

import akka.http.scaladsl.unmarshalling.Unmarshaller

import cats.syntax.either._
import cats.Show
import cats.data.NonEmptyList
import cats.syntax.show._
import io.circe._

sealed trait Currency
object Currency {
  final case object AUD extends Currency
  final case object CAD extends Currency
  final case object CHF extends Currency
  final case object EUR extends Currency
  final case object GBP extends Currency
  final case object NZD extends Currency
  final case object JPY extends Currency
  final case object SGD extends Currency
  final case object USD extends Currency

  implicit val show: Show[Currency] = Show.show {
    case AUD ⇒ "AUD"
    case CAD ⇒ "CAD"
    case CHF ⇒ "CHF"
    case EUR ⇒ "EUR"
    case GBP ⇒ "GBP"
    case NZD ⇒ "NZD"
    case JPY ⇒ "JPY"
    case SGD ⇒ "SGD"
    case USD ⇒ "USD"
  }

  implicit val showPair: Show[(Currency, Currency)] = Show.show {
    case (base, quote) => s"${base.show}${quote.show}"
  }

  def fromString(s: String): Currency = s match {
    case "AUD" | "aud" ⇒ AUD
    case "CAD" | "cad" ⇒ CAD
    case "CHF" | "chf" ⇒ CHF
    case "EUR" | "eur" ⇒ EUR
    case "GBP" | "gbp" ⇒ GBP
    case "NZD" | "nzd" ⇒ NZD
    case "JPY" | "jpy" ⇒ JPY
    case "SGD" | "sgd" ⇒ SGD
    case "USD" | "usd" ⇒ USD
  }

  val values: NonEmptyList[Currency] = NonEmptyList.fromListUnsafe(List(AUD, CAD, CHF, EUR, GBP, NZD, JPY, SGD, USD))

  val combinations: NonEmptyList[Rate.Pair] =
    for {
      base  <- values
      quote <- NonEmptyList.fromListUnsafe(values.filterNot(_ == base))
    } yield Rate.Pair(base, quote)

  implicit val encoder: Encoder[Currency] =
    Encoder.instance[Currency] { show.show _ andThen Json.fromString }

  implicit val decoder: Decoder[Currency] =
    Decoder.decodeString.emap(str =>
      Either
        .catchNonFatal(fromString(str))
        .leftMap(e => s"failed to decode $str due to, ${e.getMessage}")
    )

  implicit val currency =
    Unmarshaller.strict[String, Currency](Currency.fromString)
}
