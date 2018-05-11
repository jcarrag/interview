package forex

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.RouteTestTimeout
import akka.testkit.TestDuration
import cats.syntax.show._
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
import forex.domain.{Currency, Price}
import forex.interfaces.api.rates.Protocol.{ConvertApiResponse, GetApiResponse}
import forex.interfaces.api.rates.Routes
import org.scalatest.Matchers

import scala.concurrent.duration._

class ForexIntegrationTest extends AppSpec[Routes] with Matchers with ScalatestRouteTest {
  implicit val timeout = RouteTestTimeout(3.seconds.dilated)

  val route = app.route

  "Forex Routes" should {
    "have api to request currency exchange" in {
      val request = HttpRequest(
        method = HttpMethods.GET,
        uri = Uri("/quote").withQuery(Query("from=USD&to=EUR"))
      )

      request ~> route ~> check {
        status shouldEqual StatusCodes.OK
        val response: GetApiResponse = entityAs[GetApiResponse]
        response.from.show shouldBe "USD"
        response.to.show shouldBe "EUR"
      }
    }

    "have api to convert currency" in {
      val convertReq = HttpRequest(
        method = HttpMethods.GET,
        uri = Uri("/convert").withQuery(Query("from=USD&to=EUR&quantity=100"))
      )
      val getReq = HttpRequest(
        method = HttpMethods.GET,
        uri = Uri("/quote").withQuery(Query("from=USD&to=EUR"))
      )

      convertReq ~> route ~> check {
        status shouldEqual StatusCodes.OK
        val convertResponse: ConvertApiResponse = entityAs[ConvertApiResponse]
        getReq ~> route ~> check {
          status shouldEqual StatusCodes.OK
          val getResponse: GetApiResponse = entityAs[GetApiResponse]
          val expectedValue = Price(100 * getResponse.price.value)
          convertResponse.value shouldBe expectedValue
          convertResponse.text.value shouldBe s"100 USD is worth ${expectedValue.value} EUR"
        }
      }
    }

    "have api to expose supported currency" in {
      Get("/symbol") ~> route ~> check {
        status shouldEqual StatusCodes.OK
        entityAs[List[Currency]] should contain theSameElementsAs(Currency.values.toList)
      }
    }
  }
}
