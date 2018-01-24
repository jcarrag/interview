package users.services.client

import java.time.OffsetDateTime

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveEncoder}
import io.circe.generic.extras.semiauto.{deriveUnwrappedEncoder, deriveUnwrappedDecoder}
import io.circe.parser._
import io.circe.syntax._

import users.services.usermanagement.Error

sealed trait ClientDomain extends Any

object ClientDomain {
  final case class Done() extends ClientDomain
  final case class EmailAddress(value: String) extends AnyVal with ClientDomain
  final case class UserName(value: String) extends AnyVal with ClientDomain
  final case class Password(value: String) extends AnyVal with ClientDomain
  final case class User(
    id: User.Id,
    userName: UserName,
    emailAddress: EmailAddress
  ) extends ClientDomain

  final case object User {
    case class Id(value: String) extends AnyVal with ClientDomain
  }

  object Encoders {
    implicit val emailEncoder: Encoder[EmailAddress] = deriveUnwrappedEncoder
    implicit val userNameEncoder: Encoder[UserName] = deriveUnwrappedEncoder
    implicit val userIdEncoder: Encoder[User.Id] = deriveUnwrappedEncoder
    implicit val doneEncoder: Encoder[Done] = deriveEncoder
    implicit val userEncoder: Encoder[User] = deriveEncoder
    implicit val offsetDateTimeEncoder: Encoder[OffsetDateTime] = Encoder.encodeString.contramap[OffsetDateTime](_.toString)
  }

  object Decoders {
    implicit val emailDecoder: Decoder[EmailAddress] = deriveUnwrappedDecoder
    implicit val userNameDecoder: Decoder[UserName] = deriveUnwrappedDecoder
    implicit val passwordDecoder: Decoder[Password] = deriveUnwrappedDecoder
    implicit val userIdDecoder: Decoder[User.Id] = deriveUnwrappedDecoder
  }
}
