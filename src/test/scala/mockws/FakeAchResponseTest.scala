package mockws

import org.apache.commons.io.IOUtils
import org.scalatest.{FunSpec, Matchers}
import play.api.libs.iteratee.Enumerator
import play.api.mvc.{ResponseHeader, Result}

class FakeAchResponseTest extends FunSpec with Matchers {

  describe("typical 200 response") {
    val bodyString = "response body"
    val charset = "utf-8"
    val bodyBytes = bodyString.getBytes(charset)
    val contentType = s"text/plain; charset=$charset"

    val response = buildFakeAchResponse(
      status = 200,
      headers = Map("Content-Type" -> contentType),
      body = bodyBytes
    )

    it("returns expected values for all methods") {
      response.getResponseBodyAsByteBuffer.array() shouldBe bodyBytes

      response.getResponseBodyExcerpt(0) shouldBe ""
      response.getResponseBodyExcerpt(1) shouldBe "r"

      response.getResponseBodyExcerpt(0, null) shouldBe ""
      response.getResponseBodyExcerpt(1, null) shouldBe "r"

      response.getStatusCode shouldBe 200

      response.getResponseBodyAsBytes shouldBe bodyBytes

      IOUtils.toByteArray(response.getResponseBodyAsStream) shouldBe bodyBytes

      response.isRedirected shouldBe false

      response.getCookies shouldBe empty

      response.getHeader("Content-Type") shouldBe contentType
      response.getHeader("invalid") shouldBe null

      response.getHeaders should have size 1

      response.getStatusCode shouldBe 200

      response.getStatusText shouldBe "200 OK"

      response.hasResponseHeaders shouldBe true

      response.getResponseBody(charset) shouldBe bodyString

      response.getResponseBody shouldBe bodyString

      response.hasResponseBody shouldBe true

      response.getContentType shouldBe contentType

      response.hasResponseStatus shouldBe true

      a[NotImplementedError] shouldBe thrownBy(response.getUri)
    }
  }

  private def buildFakeAchResponse(
    status: Int = 200,
    headers: Map[String, String] = Map.empty,
    body: Array[Byte] = Array.empty
    ) = {
    new FakeAhcResponse(
      result = new Result(new ResponseHeader(status, headers), Enumerator(body)),
      body = body
    )
  }
}
