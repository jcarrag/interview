package forex.services.oneforge

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer

import cats.data.EitherT
import cats.data.NonEmptyList
import cats.implicits._

import com.typesafe.scalalogging._
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
import forex.config.OneForgeConfig
import forex.domain._
import forex.services.oneforge.Responses.QuoteResponse
import io.circe.{Decoder, Json}
import io.circe
import monix.cats._
import monix.eval.Task
import org.atnos.eff._
import org.atnos.eff.addon.monix.task._


object Interpreters {
  def dummy[R](
      implicit
      m1: _task[R]
  ): Algebra[Eff[R, ?]] = new Dummy[R]

  def live[R](
      oneForgeConfig: OneForgeConfig
  )(
      implicit
      m1: _task[R],
      system: ActorSystem,
      mat: Materializer
  ): Algebra[Eff[R, ?]] = new Live[R](oneForgeConfig)
}

final class Dummy[R] private[oneforge] (
    implicit
    m1: _task[R]
) extends Algebra[Eff[R, ?]] {
  override def get(
      pair: Rate.Pair
  ): Eff[R, Error Either Rate] =
    for {
      result ← fromTask(Task.now(Rate(pair, Price(BigDecimal(100)), Timestamp.now)))
    } yield result.asRight

  override def gets(
    pairs: NonEmptyList[Rate.Pair]
  ): Eff[R, Error Either NonEmptyList[Rate]] =
    for {
      result <- fromTask(Task.now(pairs.map(p => Rate(p, Price(BigDecimal(100)), Timestamp.now))))
    } yield result.asRight
}

final class Live[R] private[oneforge] (
    oneForgeConfig: OneForgeConfig
)(
    implicit
    m1: _task[R],
    system: ActorSystem,
    mat: Materializer
) extends Algebra[Eff[R, ?]] with LazyLogging {
  import monix.execution.Scheduler.Implicits.global
  private val http = Http()

  private def errorOrEntity[T: Decoder](js: Json): String Either T = {
    (js \\ "message").map(_.as[String]) match {
      case Nil => js.as[T].leftMap(_.show)
      case xs  =>
        xs.foldMap(identity) match {
          case Left(decodeErr) => decodeErr.message.asLeft[T]
          case Right(errMsg) => errMsg.asLeft[T]
        }

    }
  }

  private def getRates(
      pairs: NonEmptyList[Rate.Pair]
  ): Eff[R, Error Either NonEmptyList[Rate]] = {

    val request =
      HttpRequest(
        method = HttpMethods.GET,
        uri = Uri(oneForgeConfig.routes.quote)
          .withQuery(Query(s"api_key=${oneForgeConfig.apiKey}" +
                           s"&pairs=${pairs.toList.map(_.show).mkString(",")}"))
      )
    logger.debug(s"getRates, with request: $request")
    fromTask(Task.deferFuture(http.singleRequest(request)).flatMap {
      case HttpResponse(status, _, entity, _) if status == StatusCodes.OK =>
        logger.debug(s"getRates - response($status, $entity)")
        val resp = Task.deferFuture(Unmarshal(entity).to[Json]).map(_.asRight[String])
        (for {
          response <- EitherT(resp)
          quotes   <- EitherT(Task.pure(errorOrEntity[NonEmptyList[QuoteResponse]](response)))
        } yield quotes.map(Rate.fromQuoteResponse)).leftMap(Error.custom).value

      case response =>
        logger.debug(s"${request} failed with $response")
        Task.pure(
          Error.custom(s"${request} failed with $response")
            .raiseError[Error Either ?, NonEmptyList[Rate]]
        )
    })
  }

  override def get(
      pair: Rate.Pair
  ): Eff[R, Error Either Rate] =
    for {
      errorOrRate <- getRates(pair.pure[NonEmptyList])
    } yield errorOrRate.map(_.head)

  override def gets(
      pairs: NonEmptyList[Rate.Pair]
  ): Eff[R, Error Either NonEmptyList[Rate]] =
    getRates(pairs)

}
