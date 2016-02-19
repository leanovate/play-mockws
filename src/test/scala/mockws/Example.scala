package mockws

import org.scalatest.{OptionValues, FreeSpec, Matchers}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws.WSClient
import play.api.mvc.Action
import play.api.mvc.Results._
import play.api.test.Helpers._

import scala.concurrent.Future
import scala.util.Try

class Example extends FreeSpec with Matchers with OptionValues {


  object ImplementationToTest {

    // GatewayToTest simulates an gateway implementation we want to test

    object GatewayToTest {
      val userServiceUrl = "http://userservice"
    }

    /** @param ws [[WSClient]] as dependency injection */
    class GatewayToTest(ws: WSClient) {

      import GatewayToTest.userServiceUrl

      /** @return age of the user if known */
      def age(userId: Long): Future[Option[Int]] =
        ws.url(s"$userServiceUrl/users/$userId/age").get().map {
          case response if response.status == 200 => Try(response.body.toInt).toOption
          case _ => None
        }

    }

  }


  trait TestScope {

    import ImplementationToTest._

    val userServiceUrl = GatewayToTest.userServiceUrl

    def userRoute: Route

    def ageResponse(userId: Long) = {
      // we initialize a mock WS with the defined route
      val ws = MockWS(userRoute)

      // we inject the MockWS into GatewayToTest
      val testedGateway = new GatewayToTest(ws)
      try await(testedGateway.age(userId))
      finally ws.close()
    }
  }


  // and we can test the implementation of GatewayToTest

  "GatewayToTest.age should" - {
    "return None" - {
      "when the user service does not know the user" in new TestScope {
        override val userRoute = Route {
          case (GET, u) if u == s"$userServiceUrl/users/23/age" => Action {
            NotFound("user 23 not known")
          }
        }
        ageResponse(userId = 23) shouldEqual None
      }

      "when the user service response is not an Integer" in new TestScope {
        override val userRoute = Route {
          case (GET, u) if u == s"$userServiceUrl/users/27/age" => Action {
            Ok("crap")
          }
        }
        ageResponse(userId = 27) shouldEqual None
      }
    }

    "return the age of the user" in new TestScope {
      override val userRoute = Route {
        case (GET, u) if u == s"$userServiceUrl/users/5/age" => Action {
          Ok("67")
        }
      }
      ageResponse(userId = 5).value shouldEqual 67
    }
  }

}
