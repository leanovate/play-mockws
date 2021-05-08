package mockws

import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.util.ByteString
import mockws.MockWSHelpers._
import org.scalatest.FunSuite
import org.scalatest.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.http.HttpEntity
import play.api.libs.ws.WSClient
import play.api.mvc.MultipartFormData.DataPart
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.MultipartFormData.Part
import play.api.mvc.Results._
import play.api.mvc.ResponseHeader
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext.Implicits._

/**
 * Tests that [[MockWS]] simulates a WS client, in particular the methods involving authentication
 */
class StreamingTest extends FunSuite with Matchers with ScalaCheckPropertyChecks {

  test("mock WS simulates a streaming") {

    def testedController(ws: WSClient) = Action.async {
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

  test("mock WS supports streaming of MultipartFormData") {
    val ws = MockWS { case (PUT, "/") =>
      Action { request =>
        request.body.asMultipartFormData match {
          case None       => InternalServerError("error")
          case Some(data) => Ok(data.dataParts.toList.sortBy(_._1).mkString(", "))
        }
      }
    }

    val fileData: Source[Part[Source[ByteString, _]], NotUsed] = Source(
      FilePart("file", "", Some(BINARY), Source.single(ByteString("test"))) ::
        DataPart("key 1", "data 1") ::
        DataPart("key 2", "data 2") ::
        Nil
    )

    val response = await(ws.url("/").put(fileData))
    response.body shouldEqual "(key 1,Vector(data 1)), (key 2,Vector(data 2))"
    ws.close()
  }

  test("mock WS supports method in stream") {
    def testedController(ws: WSClient) = Action.async {
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
    val inputWords: Seq[String]             = Seq() ++ req.body.asMultipartFormData.toSeq.flatMap(_.dataParts("k1"))
    val returnWords                         = Seq(req.method + ": ") ++ inputWords
    val outputStream: Source[ByteString, _] = Source(returnWords.map(v => ByteString(v)))

    Result(
      header = ResponseHeader(200),
      body = HttpEntity.Streamed(outputStream, None, None)
    )
  }

  test("receive a stream of back what we sent as [POST]") {
    val content = Source(Seq("hello,", " this", " is", " world")).map(v => DataPart("k1", v))
    val ws = MockWS { case (POST, "/post") =>
      streamBackAction
    }

    await(ws.url("/post").post(content)).body shouldEqual "POST: hello, this is world"
    ws.close()
  }

  test("receive a stream of back what we sent as [PUT]") {
    val content = Source(Seq("hello,", " this", " is", " world")).map(v => DataPart("k1", v))
    val ws = MockWS { case (PUT, "/put") =>
      streamBackAction
    }

    await(ws.url("/put").put(content)).body shouldEqual "PUT: hello, this is world"
    ws.close()
  }

  test("receive a stream of back what we sent as  [PATCH]") {
    val content = Source(Seq("hello,", " this", " is", " world")).map(v => DataPart("k1", v))
    val ws = MockWS { case (PATCH, "/patch") =>
      streamBackAction
    }

    await(ws.url("/patch").patch(content)).body shouldEqual "PATCH: hello, this is world"
    ws.close()
  }
}
