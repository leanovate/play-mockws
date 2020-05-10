package mockws

import play.api.mvc.Cookie
import play.api.mvc.Results._
import play.api.test.Helpers._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class CookieTest extends AnyFunSuite with Matchers with MockWSHelpers {

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
      Symbol("value")("1"),
      Symbol("maxAge")(Some(3600)),
      Symbol("path")(Some("/account")),
      Symbol("domain")(Some("https://www.example.com")),
      Symbol("secure")(true)
    )

    response.header("test") shouldBe Some("yo")

    ws.close()
  }
}
