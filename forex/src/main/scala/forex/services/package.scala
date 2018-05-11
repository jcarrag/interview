package forex

package object services {

  type PriceCache[F[_]] = cache.Algebra[F]
  final val PriceCache = cache.PriceCache
  type OneForge[F[_]] = oneforge.Algebra[F]
  final val OneForge = oneforge.Interpreters
  type OneForgeError = oneforge.Error
  final val OneForgeError = oneforge.Error

}
