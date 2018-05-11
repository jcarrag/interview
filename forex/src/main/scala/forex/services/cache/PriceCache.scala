package forex.services.cache

import akka.actor.ActorSystem
import akka.http.caching.LfuCache
import akka.http.caching.scaladsl.Cache
import cats.data.NonEmptyList
import cats.syntax.option._
import cats.syntax.show._
import cats.syntax.eq._
import com.typesafe.scalalogging.LazyLogging
import forex.domain.Price
import forex.domain.Rate
import forex.domain.Rate.Pair
import forex.services.oneforge.Error
import monix.eval.Task
import org.atnos.eff.Eff
import org.atnos.eff.addon.monix.task._

import scala.concurrent.Promise
import scala.util.Try

object PriceCache {
  def ttlCache[R](
      implicit
      m1: _task[R],
      system: ActorSystem
  ): Algebra[Eff[R, ?]] = new PriceCache[R]
}

final class PriceCache[R] private[cache](
    implicit
    m1: _task[R],
    system: ActorSystem
) extends Algebra[Eff[R, ?]] with LazyLogging {
  import forex.domain.Currency.showPair
  private val cache: Cache[String, Price] = LfuCache.apply[String, Price]

  private def createKey(pair: Pair): String = pair.tupled.show

  def get(
      pair: Pair
  ): Eff[R, Option[Price]] = {
    fromTask(
      cache
        .get(createKey(pair))
        .map(Task.deferFuture(_))
        .fold(Task.now(none[Price]))(_.map(_.some))
    )
  }


  /**
    * Pre-create promise for each combination of pairs, which will be completed after oneForget.gets is done
    * @param combinations
    * @param tasks
    */
  def createPromisedPrices(
      combinations: NonEmptyList[Rate.Pair]
  ): Eff[R, (Rate) => Unit] = {
    val pairToPromisedPrice: Map[Rate.Pair, Promise[Price]] =
      combinations.toList.map { pair =>
        val promise = Promise[Price]
        cache.apply(createKey(pair), () => promise.future)

        (pair -> promise)
      }.toMap

    Eff.pure{ (rate: Rate) =>
      pairToPromisedPrice
        .get(rate.pair)
        .map(promise => promise.complete(Try(rate.price)))
    }
  }

}
