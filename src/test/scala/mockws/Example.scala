package mockws

import org.scalatest.{FreeSpec, Matchers}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws.WSClient
import play.api.mvc.Action
import play.api.mvc.Results._
import play.api.test.Helpers._

import scala.concurrent.Future
import scala.util.Try

class Example extends FreeSpec with Matchers {

  
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

  import GatewayToTest.userServiceUrl


  // we can define several routes

  val userNotKnown: MockWS.Routes = {
    case (GET, u) if u == s"$userServiceUrl/users/23/age" => Action {
      NotFound("user 23 not known")
    }
  }
  val crapResponse: MockWS.Routes = {
    case (GET, u) if u == s"$userServiceUrl/users/27/age" => Action {
      Ok("crap")
    }
  }
  val goodResponse: MockWS.Routes = {
    case (GET, u) if u == s"$userServiceUrl/users/5/age" => Action {
      Ok("67")
    }
  }


  // let's combine all these routes together

  val ws = MockWS(userNotKnown orElse crapResponse orElse goodResponse)


  // we inject the MockWS into GatewayToTest

  val testedGateway = new GatewayToTest(ws)
  def ageResponse(userId: Long) = await( testedGateway.age(userId) )


  // and we can test the implementation of GatewayToTest

  "GatewayToTest.age should" - {
    "return None" - {
      "when the user service does not know the user" in {
        ageResponse(userId = 23) shouldEqual None
      }

      "when the user service response is not an Integer" in {
        ageResponse(userId = 27) shouldEqual None
      }
    }

    "return the age of the user" in {
      ageResponse(userId = 5) shouldEqual Some(67)
    }
  }

}
