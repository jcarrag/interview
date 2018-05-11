package forex.services.cache

import cats.data.NonEmptyList
import forex.domain.{Price, Rate}
import forex.services.oneforge.Error
import monix.eval.Task

trait Algebra[F[_]] {
  def get(
      pair: Rate.Pair
  ): F[Option[Price]]

  def createPromisedPrices(
      combinations: NonEmptyList[Rate.Pair]
  ): F[(Rate) => Unit]
}
