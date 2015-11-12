package mockws

import org.scalatest.{FunSuite, Matchers}
import play.api.mvc.{Cookie, Action}
import play.api.mvc.Results._
import play.api.test.Helpers._

class CookieTest extends FunSuite with Matchers {

  test("A cookie can be returned from an action") {
    val ws = MockWS {
      case (_, _) => Action(
        NoContent.withCookies(
          Cookie(
            name = "session_id",
            value = "1",
            maxAge = Some(3600),
            path = "/account",
            domain = Some("https://www.example.com"),
            secure = true
          )
        )
      )
    }

    val response = await(ws.url("/").get())

    response.cookies should have size 1

    response.cookie("session_id").get should have(
      'value (Some("1")),
      'maxAge (Some(3600)),
      'path ("/account"),
      'domain ("https://www.example.com"),
      'secure (true)
    )
  }
}
