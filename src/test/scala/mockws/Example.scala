package mockws

import mockws.MockWSHelpers._
import org.scalatest.OptionValues
import play.api.libs.ws.WSClient
import play.api.mvc.Results._
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.util.Try
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

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
        case _                                  => None
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

class Example extends AnyFreeSpec with Matchers with OptionValues {

  // and we can test the implementation of GatewayToTest

  "GatewayToTest.age should" - {
    "return None" - {
      "when the user service does not know the user" in new TestScope {
        override val userRoute = Route {
          case (GET, u) if u == s"$userServiceUrl/users/23/age" =>
            Action {
              NotFound("user 23 not known")
            }
        }
        ageResponse(userId = 23) shouldEqual None
      }

      "when the user service response is not an Integer" in new TestScope {
        override val userRoute = Route {
          case (GET, u) if u == s"$userServiceUrl/users/27/age" =>
            Action {
              Ok("crap")
            }
        }
        ageResponse(userId = 27) shouldEqual None
      }
    }

    "return the age of the user" in new TestScope {
      override val userRoute = Route {
        case (GET, u) if u == s"$userServiceUrl/users/5/age" =>
          Action {
            Ok("67")
          }
      }
      ageResponse(userId = 5).value shouldEqual 67
    }
  }

}

/**
 * Just a demonstration that you can also import the MockWSHelpers and do not have to mix it in as trait.
 *
 * {{{
 * import mockws.MockWSHelpers._
 * }}}
 */
class ExampleWithImportMockWSHelpers extends AnyFreeSpec with Matchers with OptionValues {

  // Just a simple import and Actions work, no mixins
  import mockws.MockWSHelpers._

  "GatewayToTest.age should" - {
    "work properly when we import the MockWSHelpers (and do not use it as trait)" in new TestScope {
      override val userRoute = Route {
        case (GET, u) if u == s"$userServiceUrl/users/23/age" =>
          Action {
            NotFound("user 23 not known")
          }
      }
      ageResponse(userId = 23) shouldEqual None
    }
  }

}
