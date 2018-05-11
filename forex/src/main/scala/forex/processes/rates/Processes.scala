package forex.processes.rates

import cats.data.EitherT
import cats.implicits._
import cats.Monad
import cats.data.NonEmptyList
import com.typesafe.scalalogging.LazyLogging
import forex.domain._
import forex.services._
import forex.services.oneforge.{Error => OneForgeError}

object Processes {
  def apply[F[_]]: Processes[F] =
    new Processes[F] {}
}

trait Processes[F[_]] extends LazyLogging{
  import messages._
  import converters._
  private def cachedGet(
      pair: Rate.Pair
  )(
      implicit
      M: Monad[F],
      OneForge: OneForge[F],
      priceCache: PriceCache[F]
  ): F[Error Either Rate] =
    (for {
      cached <- EitherT(priceCache.get(pair).map(_.asRight[OneForgeError]))
      price  <- cached match {
        case Some(p) => EitherT(M.pure(p.asRight[OneForgeError]))
        case None =>
          for {
            promisedPrices <- EitherT(priceCache.createPromisedPrices(Currency.combinations).map(_.asRight[OneForgeError]))
            rates          <- EitherT(OneForge.gets(Currency.combinations))
            _              = rates.map(r => promisedPrices(r))
            maybePrice     <- EitherT(priceCache.get(pair).map(_.asRight[OneForgeError]))
            price          <- EitherT(M.pure(maybePrice.toRight(oneforge.Error.custom(s"something went wrong with caching/1forge request for ${pair}"))))
          } yield price
      }
    } yield Rate(pair, price, Timestamp.now)).leftMap(toProcessError).value

  def get(
      request: GetRequest
  )(
      implicit
      M: Monad[F],
      OneForge: OneForge[F],
      priceCache: PriceCache[F]
  ): F[Error Either Rate] =
    cachedGet(Rate.Pair(request.from, request.to))

  def convert(
      request: ConvertRequest
  )(
      implicit
      M: Monad[F],
      OneForge: OneForge[F],
      priceCache: PriceCache[F]
  ): F[Error Either Conversion] = {
    for {
      rate <- cachedGet(Rate.Pair(request.from, request.to))
    } yield rate.map{ r =>
      val value = request.quantity applyConversion r.price

      Conversion(
      value = value,
      text = Text.describe(request.quantity, request.from, value, request.to),
      timestamp = r.timestamp
    )}
  }

  def symbols(
      implicit
      M: Monad[F]
  ): F[NonEmptyList[Currency]] =
    M.pure(Currency.values)
}
