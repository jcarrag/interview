package users.services.client

import io.circe._
import io.circe.syntax._
import io.circe.parser.decode

import akka.http.scaladsl.model.{HttpEntity, ContentTypeRange}
import akka.http.scaladsl.model.ContentTypes.`application/json`

import scala.concurrent.Future

object CirceSupport {
  import akka.http.scaladsl.unmarshalling._
  import akka.http.scaladsl.marshalling._
  import akka.http.scaladsl.model.MediaTypes.`application/json`

  private def jsonContentTypes: List[ContentTypeRange] =
    List(`application/json`)

  implicit final def unmarshaller[A: Decoder]: FromEntityUnmarshaller[A] = {
    Unmarshaller.stringUnmarshaller
      .forContentTypes(jsonContentTypes: _*)
      .flatMap { ctx => mat => json =>
        decode[A](json).fold(Future.failed, Future.successful)
      }
  }

  implicit final def marshaller[A: Encoder]: ToEntityMarshaller[A] = {
    Marshaller.withFixedContentType(`application/json`) { a =>
      HttpEntity(`application/json`, a.asJson.noSpaces)
    }
  }
}
