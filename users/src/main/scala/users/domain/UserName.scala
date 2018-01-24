package users.domain

import cats.kernel.Eq

final case class UserName(value: String) extends AnyVal with Domain

object UserName {
  implicit val eq: Eq[UserName] =
    Eq.fromUniversalEquals
}
