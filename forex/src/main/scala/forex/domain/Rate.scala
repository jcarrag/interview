package forex.domain

import cats.Show
import cats.kernel.Eq
import cats.syntax.show._
import forex.services.oneforge.Responses.QuoteResponse
import io.circe._
import io.circe.generic.semiauto._

case class Rate(
    pair: Rate.Pair,
    price: Price,
    timestamp: Timestamp
)

object Rate {
  def fromQuoteResponse(quote: QuoteResponse): Rate =
    Rate(Pair.tupled(quote.symbol), quote.price, quote.timestamp)

  implicit val encoder: Encoder[Rate] =
    deriveEncoder[Rate]

  final case class Pair(
      from: Currency,
      to: Currency
  ) {
    def tupled: (Currency, Currency) = (this.from, this.to)
  }

  object Pair {
    def tupled(pair: (Currency, Currency)): Pair =
      (Pair.apply _).tupled(pair)

    implicit val eq: Eq[Pair] = Eq.instance[Pair] { (l, r) =>
      l.from == r.from && l.to == r.to
    }

    implicit val pairShow: Show[Pair] = Show.show {
      p => s"${p.from.show}${p.to.show}"
    }

    implicit val encoder: Encoder[Pair] =
      deriveEncoder[Pair]
  }
}
