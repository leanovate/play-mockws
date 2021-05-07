package mockws

import org.scalatest.FunSuite
import org.scalatest.Matchers
import play.api.mvc.Results._
import play.api.test.Helpers._
import play.api.libs.ws.WSRequestExecutor

class RequestFilterTest extends FunSuite with Matchers with MockWSHelpers {

  test("A header can be added by a filter") {
    val ws = MockWS {
      case (_, _) =>
        Action { req =>
          NoContent.withHeaders("test" -> req.headers.get("test").getOrElse("missing"))
        }
    }

    val headerValue = "filter applied"

    val wsReq = ws
      .url("/")
      .withRequestFilter { executor =>
        WSRequestExecutor { req =>
          executor(req.addHttpHeaders("test" -> headerValue))
        }
      }
    val response = await(wsReq.get())

    response.header("test") shouldBe Some(headerValue)

    ws.close()
  }
}
