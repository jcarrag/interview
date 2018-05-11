package forex.services.oneforge

import forex.domain._
import cats.data.NonEmptyList

trait Algebra[F[_]] {
  def get(
      pair: Rate.Pair
  ): F[Error Either Rate]

  def gets(
      pairs: NonEmptyList[Rate.Pair]
  ): F[Error Either NonEmptyList[Rate]]
}
