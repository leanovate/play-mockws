package mockws

import akka.stream.scaladsl.{Concat, Source}
import akka.util.ByteString
import mockws.MockWSHelpers._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.http.HttpEntity
import play.api.libs.ws.DefaultBodyWritables._
import play.api.libs.ws.StandaloneWSClient
import play.api.mvc.ResponseHeader
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.collection.immutable.Seq
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

    await(
      ws.url("/get")
        .get()
    ).body shouldEqual "hello, world"
    ws.close()
  }

  val streamBackAction = Action { req =>
    val requestBody                         = Source.single(req.body.asRaw.flatMap(_.asBytes()).getOrElse(ByteString("")))
    val methodAsStream                      = Source.single(ByteString(req.method + ": "))
    val outputStream: Source[ByteString, _] = Source.combine(methodAsStream, requestBody)(Concat(_))

    Result(
      header = ResponseHeader(200),
      body = HttpEntity.Streamed(outputStream, None, None)
    )
  }

  test("receive a stream of back what we sent as [POST]") {
    val content = Source.single(ByteString("hello, this is world"))
    val ws = MockWS { case (POST, "/post") =>
      streamBackAction
    }

    await(ws.url("/post").post(content)).body shouldEqual "POST: hello, this is world"
    ws.close()
  }

  test("receive a stream of back what we sent as [PUT]") {
    val content = Source.single(ByteString("hello, this is world"))
    val ws = MockWS { case (PUT, "/put") =>
      streamBackAction
    }

    await(ws.url("/put").put(content)).body shouldEqual "PUT: hello, this is world"
    ws.close()
  }

  test("receive a stream of back what we sent as  [PATCH]") {
    val content = Source.single(ByteString("hello, this is world"))
    val ws = MockWS { case (PATCH, "/patch") =>
      streamBackAction
    }

    await(ws.url("/patch").patch(content)).body shouldEqual "PATCH: hello, this is world"
    ws.close()
  }
}
