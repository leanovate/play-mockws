package mockws

import org.scalatest.FunSuite
import org.scalatest.Matchers
import play.api.mvc.Cookie
import play.api.mvc.Results._
import play.api.test.Helpers._

class CookieTest extends FunSuite with Matchers with MockWSHelpers {

  test("A cookie can be returned from an action") {
    val ws = MockWS {
      case (_, _) =>
        Action(
          NoContent
            .withCookies(
              Cookie(
                name = "session_id",
                value = "1",
                maxAge = Some(3600),
                path = "/account",
                domain = Some("https://www.example.com"),
                secure = true
              )
            )
            .withHeaders("test" -> "yo")
        )
    }

    val response = await(ws.url("/").get())

    response.cookies should have size 1

    response.cookie("session_id").get should have(
      'value ("1"),
      'maxAge (Some(3600)),
      'path (Some("/account")),
      'domain (Some("https://www.example.com")),
      'secure (true)
    )

    response.header("test") shouldBe Some("yo")

    ws.close()
  }
}
