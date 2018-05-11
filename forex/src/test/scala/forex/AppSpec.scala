package forex

import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.Eval
import forex.config._
import org.zalando.grafter._
import org.scalatest.{BeforeAndAfterAll, EitherValues, WordSpec}
import org.zalando.grafter.syntax.rewriter._

abstract class AppSpec[A: ApplicationConfigReader] extends WordSpec with ScalatestRouteTest with EitherValues {
  val appConfig = pureconfig.loadConfig[ApplicationConfig]("app").right.value
  val app: A = {
    val application = configure[A](appConfig).singletons
    Rewriter
      .startAll(application)
      .flatMap {
        case results if results.forall(_.success) =>
          Eval.now(())
      }.value
    application
  }

  override protected def afterAll() {
    Rewriter.stopAll(app)
    cleanUp()
    super.afterAll()
  }
}
