package forex.main

import forex.config._
import forex.{ services ⇒ s }
import forex.{ processes ⇒ p }
import org.zalando.grafter.macros._

@readerOf[ApplicationConfig]
case class Processes(
  oneForgeConfig: OneForgeConfig,
  actorSystems: ActorSystems
) {
  import actorSystems._

  implicit final val _oneForge: s.OneForge[AppEffect] =
    s.OneForge.live[AppStack](oneForgeConfig)

  implicit final val _cache: s.PriceCache[AppEffect] =
    s.PriceCache.ttlCache[AppStack]

  final val Rates = p.Rates[AppEffect]

}
