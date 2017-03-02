package mockws

import akka.stream.scaladsl.Source
import akka.util.ByteString
import org.mockito.Mockito._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FunSuite, Matchers}
import play.api.http.HttpEntity
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import play.api.libs.ws.{WSAuthScheme, WSClient, WSSignatureCalculator}
import play.api.mvc.BodyParsers.parse
import play.api.mvc.Results._
import play.api.mvc.{Action, ResponseHeader, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.collection.immutable.Seq
import scala.concurrent.duration._

/**
 * Tests that [[MockWS]] simulates a WS client, in particular the methods involving authentication
 */
class StreamingTest extends FunSuite with Matchers with PropertyChecks {

  test("mock WS simulates a streaming") {

    def testedController(ws: WSClient) = Action.async {
      ws.url("/").stream().map { resp =>
        Result(
          header = ResponseHeader(resp.headers.status, resp.headers.headers.mapValues(_.head)),
          body = HttpEntity.Streamed(resp.body, None, None))
      }
    }

    val ws = MockWS {
      case (GET, "/") => Action {
        val body: Source[ByteString, _] = Source(Seq("first", "second", "third").map(ByteString.apply))
        Result(
          header = ResponseHeader(201, Map("x-header" -> "x-value")),
          body = HttpEntity.Streamed(body, None, None))
      }
    }
    import ws.materializer

    val response = testedController(ws).apply(FakeRequest())
    status(response) shouldEqual CREATED
    contentAsString(response) shouldEqual "firstsecondthird"
    header("x-header", response) shouldEqual Some("x-value")
    ws.close()
  }

  test("mock WS supports method in stream") {
    def testedController(ws: WSClient) = Action.async {
      ws.url("/").withMethod("POST").stream().map { resp =>
        Result(
          header = ResponseHeader(resp.headers.status, resp.headers.headers.mapValues(_.head)),
          body = HttpEntity.Streamed(resp.body, None, None)
        )
      }
    }

    val ws = MockWS {
      case (POST, "/") => Action {
        val body: Source[ByteString, _] = Source(Seq("first", "second", "third").map(ByteString.apply))
        Result(
          header = ResponseHeader(201, Map("x-header" -> "x-value")),
          body = HttpEntity.Streamed(body, None, None)
        )
      }
    }
    import ws.materializer

    val response = testedController(ws).apply(FakeRequest())
    status(response) shouldEqual CREATED
    contentAsString(response) shouldEqual "firstsecondthird"
    header("x-header", response) shouldEqual Some("x-value")
    ws.close()
  }

  test("should pass through all elements of a Source") {
    val content = Source(Seq("hello, ", "world").map(ByteString(_)))

    val ws = MockWS {
      case (GET, "/get") ⇒ Action {
        Result(
          header = ResponseHeader(200),
          body = HttpEntity.Streamed(content, None, None)
        )
      }
    }

    await(ws
      .url("/get")
      .get()).body shouldEqual "hello, world"
    ws.close()
  }
}
