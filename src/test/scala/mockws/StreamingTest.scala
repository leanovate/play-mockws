package mockws

import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.util.ByteString
import org.scalatest.prop.PropertyChecks
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}
import play.api.http.HttpEntity
import play.api.mvc.MultipartFormData.{DataPart, FilePart, Part}
import play.api.mvc.{Action, ResponseHeader, Result}
import play.api.mvc.Results._
import play.api.test.Helpers._
import play.api.test.TestServer

import scala.collection.immutable.Seq

/**
  * Tests that [[MockWS]] simulates a WS client, in particular the methods involving authentication
  */
class StreamingTest extends FunSuite with Matchers with PropertyChecks with BeforeAndAfterAll {

  private val testServer: TestServer = new TestServer(port = 0)
  override protected def beforeAll(): Unit = {
    super.beforeAll()
    testServer.start()
  }

  override protected def afterAll(): Unit = {
    testServer.stop()
    super.afterAll()
  }


  test("mock WS supports streaming of MultipartFormData") {
    val ws = MockWS {
      case (PUT, "/") =>
        Action { request ⇒
          request.body.asMultipartFormData match {
            case None ⇒ InternalServerError("error")
            case Some(data) ⇒ Ok(data.dataParts.mkString(", "))
          }
        }
    }

    val fileData: Source[Part[Source[ByteString, _]], NotUsed] = Source(
      FilePart("file", "", Some(BINARY), Source.single(ByteString("test"))) ::
        DataPart("key 1", "data 1") ::
        DataPart("key 2", "data 2") ::
        Nil)

    val response = await(ws.url("/").put(fileData))
    response.body shouldEqual "key 2 -> Vector(data 2), key 1 -> Vector(data 1)"
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

  val streamBackAction = Action {
    req =>

      val inputWords: Seq[String] = Seq() ++ req.body.asMultipartFormData.toSeq.flatMap(_.dataParts("k1"))
      val returnWords = Seq(req.method + ": ") ++ inputWords
      val outputStream: Source[ByteString, _] = Source(returnWords.map(v => ByteString(v)))

      Result(
        header = ResponseHeader(200),
        body = HttpEntity.Streamed(outputStream, None, None)
      )
  }

  test("receive a stream of back what we sent as [POST]") {
    val content = Source(Seq("hello,", " this", " is", " world")).map(v => DataPart("k1", v))
    val ws = MockWS {
      case (POST, "/post") ⇒
        streamBackAction
    }

    await(ws.url("/post").post(content)).body shouldEqual "POST: hello, this is world"
    ws.close()
  }

  test("receive a stream of back what we sent as [PUT]") {
    val content = Source(Seq("hello,", " this", " is", " world")).map(v => DataPart("k1", v))
    val ws = MockWS {
      case (PUT, "/put") ⇒
        streamBackAction
    }

    await(ws.url("/put").put(content)).body shouldEqual "PUT: hello, this is world"
    ws.close()
  }

  test("receive a stream of back what we sent as  [PATCH]") {
    val content = Source(Seq("hello,", " this", " is", " world")).map(v => DataPart("k1", v))
    val ws = MockWS {
      case (PATCH, "/patch") ⇒
        streamBackAction
    }

    await(ws.url("/patch").patch(content)).body shouldEqual "PATCH: hello, this is world"
    ws.close()
  }
}
