package mockws

import org.apache.pekko.NotUsed
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.util.ByteString
import mockws.MockWSHelpers._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.http.HttpEntity
import play.api.libs.ws.StandaloneWSClient
import play.api.mvc.MultipartFormData.DataPart
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.MultipartFormData.Part
import play.api.mvc.Results._
import play.api.mvc.AnyContent
import play.api.mvc.Request
import play.api.mvc.ResponseHeader
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.libs.ws.DefaultBodyWritables._

import scala.concurrent.ExecutionContext.Implicits._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

/**
 * Tests that [[MockWS]] simulates a WS client, in particular the methods involving authentication
 */
class StreamingTest extends AnyFunSuite with Matchers with ScalaCheckPropertyChecks {

  test("mock WS simulates a streaming") {

    def testedController(ws: StandaloneWSClient) = Action.async {
      ws.url("/").stream().map { resp =>
        Result(
          header = ResponseHeader(resp.status, resp.headers.map { case (k, v) => (k, v.head) }),
          body = HttpEntity.Streamed(resp.bodyAsSource, None, None)
        )
      }
    }

    val ws = MockWS { case (GET, "/") =>
      Action {
        val body: Source[ByteString, _] = Source(Seq("first", "second", "third").map(ByteString.apply))
        Result(
          header = ResponseHeader(201, Map("x-header" -> "x-value")),
          body = HttpEntity.Streamed(body, None, None)
        )
      }
    }

    val response = testedController(ws).apply(FakeRequest())
    status(response) shouldEqual CREATED
    contentAsString(response) shouldEqual "firstsecondthird"
    header("x-header", response) shouldEqual Some("x-value")
    ws.close()
  }

  test("mock WS supports method in stream") {
    def testedController(ws: StandaloneWSClient) = Action.async {
      ws.url("/").withMethod("POST").stream().map { resp =>
        Result(
          header = ResponseHeader(resp.status, resp.headers.map { case (k, v) => (k, v.head) }),
          body = HttpEntity.Streamed(resp.bodyAsSource, None, None)
        )
      }
    }

    val ws = MockWS { case (POST, "/") =>
      Action {
        val body: Source[ByteString, _] = Source(Seq("first", "second", "third").map(ByteString.apply))
        Result(
          header = ResponseHeader(201, Map("x-header" -> "x-value")),
          body = HttpEntity.Streamed(body, None, None)
        )
      }
    }

    val response = testedController(ws).apply(FakeRequest())
    status(response) shouldEqual CREATED
    contentAsString(response) shouldEqual "firstsecondthird"
    header("x-header", response) shouldEqual Some("x-value")
    ws.close()
  }

  test("should pass through all elements of a Source") {
    val content = Source(Seq("hello, ", "world").map(ByteString(_)))

    val ws = MockWS { case (GET, "/get") =>
      Action {
        Result(
          header = ResponseHeader(200),
          body = HttpEntity.Streamed(content, None, None)
        )
      }
    }

    import play.api.libs.ws.DefaultBodyReadables.readableAsString

    await(
      ws.url("/get")
        .get()
    ).body shouldEqual "hello, world"
    ws.close()
  }

  val streamBackAction = Action { (req: Request[AnyContent]) =>
    val inputWords: Seq[String]             = Seq() ++ req.body.asMultipartFormData.toSeq.flatMap(_.dataParts("k1"))
    val returnWords                         = Seq(req.method + ": ") ++ inputWords
    val outputStream: Source[ByteString, _] = Source(returnWords.map(v => ByteString(v)))

    Result(
      header = ResponseHeader(200),
      body = HttpEntity.Streamed(outputStream, None, None)
    )
  }
}
