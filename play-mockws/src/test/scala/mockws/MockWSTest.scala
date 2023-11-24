package mockws

import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicReference

import mockws.MockWSHelpers._
import org.mockito.Mockito._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.http.Status
import play.api.libs.json.Json
import play.api.libs.ws.WSAuthScheme
import play.api.libs.ws.WSResponse
import play.api.libs.ws.WSSignatureCalculator
import play.api.mvc.Result
import play.api.mvc.Results._
import play.api.test.Helpers._

import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.concurrent.duration._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import play.api.libs.ws.writeableOf_String
import play.api.libs.ws.readableAsString
import play.api.libs.ws.writeableOf_JsValue

/**
 * Tests that [[MockWS]] simulates a WS client
 */
class MockWSTest extends AnyFunSuite with Matchers with ScalaCheckPropertyChecks {

  test("mock WS simulates all HTTP methods") {
    val ws = MockWS {
      case (GET, "/get")       => Action { Ok("get ok") }
      case (POST, "/post")     => Action { Ok("post ok") }
      case (PUT, "/put")       => Action { Ok("put ok") }
      case (DELETE, "/delete") => Action { Ok("delete ok") }
      case ("PATCH", "/patch") => Action { Ok("patch ok") }
    }

    await(ws.url("/get").get()).body shouldEqual "get ok"
    await(ws.url("/post").post("")).body shouldEqual "post ok"
    await(ws.url("/put").put("")).body shouldEqual "put ok"
    await(ws.url("/delete").delete()).body shouldEqual "delete ok"
    await(ws.url("/patch").patch("")).body shouldEqual "patch ok"
    ws.close()
  }

  test("mock WS simulates the HTTP status code") {
    val ws = MockWS {
      case (GET, "/get200") => Action { Ok("") }
      case (GET, "/get201") => Action { Created("") }
      case (GET, "/get404") => Action { NotFound("") }
    }

    await(ws.url("/get200").get()).status shouldEqual OK
    await(ws.url("/get201").get()).status shouldEqual CREATED
    await(ws.url("/get404").get()).status shouldEqual NOT_FOUND
    ws.close()
  }

  test("mock WS works with explicit request") {
    val ws = MockWS { case (GET, "/blah") =>
      Action { request =>
        Ok(request.method)
      }
    }
    await(ws.url("/blah").get()).status shouldEqual OK
    ws.close()
  }

  test("mock WS simulates a POST with a JSON payload") {
    val ws = MockWS { case (POST, "/") =>
      Action { request =>
        Ok((request.body.asJson.get \ "result").as[String])
      }
    }

    val json = Json.parse("""{"result": "OK"}""")

    val response = await(ws.url("/").post(json))
    response.status shouldEqual OK
    response.body shouldEqual "OK"
    ws.close()
  }

  test("mock WS simulates a POST with a JSON payload with a custom content type") {
    val ws = MockWS { case (POST, "/") =>
      Action(BodyParser.tolerantJson) { request =>
        Ok((request.body \ "result").as[String])
      }
    }

    val json = Json.parse("""{"result": "OK"}""")

    val response = await(ws.url("/").addHttpHeaders(CONTENT_TYPE -> "application/my-json").post(json))
    response.status shouldEqual OK
    response.body shouldEqual "OK"
    ws.close()
  }

  test("mock WS sets the response content type") {
    val ws = MockWS {
      case (GET, "/text") => Action { Ok("text") }
      case (GET, "/json") => Action { Ok(Json.parse("""{ "type": "json" }""")) }
    }

    val text = await(ws.url("/text").get())
    val json = await(ws.url("/json").get())

    text.header(CONTENT_TYPE) shouldEqual Some("text/plain; charset=utf-8")
    json.header(CONTENT_TYPE) shouldEqual Some("application/json")
    ws.close()
  }

  test("mock WS can produce JSON") {
    val ws = MockWS { case (GET, "/json") =>
      Action {
        Ok(Json.obj("field" -> "value"))
      }
    }

    val wsResponse = await(ws.url("/json").get())
    wsResponse.body shouldEqual """{"field":"value"}"""
    (wsResponse.json \ "field").asOpt[String] shouldEqual Some("value")
    ws.close()
  }

  test("mock WS can produce XML") {
    val ws = MockWS { case (GET, "/xml") =>
      Action {
        Ok(<foo><bar>value</bar></foo>)
      }
    }

    val wsResponse = await(ws.url("/xml").get())
    wsResponse.body shouldEqual "<foo><bar>value</bar></foo>"
    (wsResponse.xml \ "bar").text shouldEqual "value"
    ws.close()
  }

  test("a call to an unknown route produces default not found") {
    val ws = MockWS { case (GET, "/url") =>
      Action { Ok("") }
    }
    await(ws.url("/url2").get()).status should be(Status.NOT_FOUND)
    await(ws.url("/url").get()).status should be(Status.OK)
    ws.close()
  }

  test("a call to an unknown route produces implicit behaviour") {

    implicit val notDefinedBehaviour = RouteNotDefined(BadGateway)
    val ws = MockWS { case (GET, "/url") =>
      Action { Ok("") }
    }
    await(ws.url("/url2").get()).status should be(Status.BAD_GATEWAY)
    await(ws.url("/url").get()).status should be(Status.OK)
    ws.close()
  }

  test("mock WS supports custom response content types") {
    val ws = MockWS { case (_, _) =>
      Action {
        Ok("hello").as("hello/world")
      }
    }

    val wsResponse = await(ws.url("/").get())
    wsResponse.status shouldEqual OK
    wsResponse.header(CONTENT_TYPE) shouldEqual Some("hello/world")
    wsResponse.body shouldEqual "hello"
    ws.close()
  }

  test("mock WS supports custom request content types") {
    val ws = MockWS { case (_, _) =>
      Action { request =>
        request.contentType match {
          case Some(ct) => Ok(ct)
          case None     => BadRequest("no content type")
        }
      }
    }

    val wsResponse = await(ws.url("/").addHttpHeaders(CONTENT_TYPE -> "hello/world").get())
    wsResponse.status shouldEqual OK
    wsResponse.body shouldEqual "hello/world"
    ws.close()
  }

  test("mock WS supports query parameter") {
    forAll { (q: String, v: String) =>
      whenever(q.nonEmpty) {

        val ws = MockWS { case (GET, "/uri") =>
          Action { request =>
            request.getQueryString(q).fold[Result](NotFound) { id =>
              Ok(id)
            }
          }
        }

        val wsResponse = await(ws.url("/uri").addQueryStringParameters(q -> v).get())
        wsResponse.status shouldEqual OK
        wsResponse.body shouldEqual v
        ws.close()
      }
    }
  }

  test("mock WS supports varargs passed as immutable Seqs") {
    forAll { (q: String, v: String) =>
      whenever(q.nonEmpty) {

        val ws = MockWS { case (GET, "/uri") =>
          Action { request =>
            request.getQueryString(q).fold[Result](NotFound) { id =>
              Ok(id)
            }
          }
        }

        await(ws.url("/uri").addHttpHeaders(Seq(q -> v): _*).get())
        await(ws.url("/uri").addQueryStringParameters(Seq(q -> v): _*).get())
        ws.close()
      }
    }
  }

  test("mock WS supports method in execute") {
    val ws = MockWS {
      case (GET, "/get")       => Action { Ok("get ok") }
      case (POST, "/post")     => Action { Ok("post ok") }
      case (PUT, "/put")       => Action { Ok("put ok") }
      case (DELETE, "/delete") => Action { Ok("delete ok") }
    }

    await(ws.url("/get").withMethod("GET").execute()).body shouldEqual "get ok"
    await(ws.url("/post").withMethod("POST").execute()).body shouldEqual "post ok"
    await(ws.url("/put").withMethod("PUT").execute()).body shouldEqual "put ok"
    await(ws.url("/delete").withMethod("DELETE").execute()).body shouldEqual "delete ok"
    ws.close()
  }

  test("should not raise NullPointerExceptions on method chaining") {
    val ws = MockWS { case (GET, "/get") =>
      Action { Ok("get ok") }
    }

    await(
      ws.url("/get")
        .sign(mock(classOf[WSSignatureCalculator]))
        .withVirtualHost("bla")
        .withFollowRedirects(follow = true)
        .withAuth("user", "password", WSAuthScheme.BASIC)
        .withRequestTimeout(10.millis)
        .get()
    ).body shouldEqual "get ok"
    ws.close()
  }

  test("should not raise Exceptions when asking for the used sockets") {
    val ws = MockWS { case (GET, "/get") =>
      Action { Ok("get ok") }
    }

    val request: Future[WSResponse] = ws
      .url("/get")
      .sign(mock(classOf[WSSignatureCalculator]))
      .withVirtualHost("bla")
      .withFollowRedirects(follow = true)
      .withRequestTimeout(10.millis)
      .get()

    val response: WSResponse = await(request)

    response.body shouldEqual "get ok"

    response.underlying[play.shaded.ahc.org.asynchttpclient.Response].getLocalAddress shouldBe InetSocketAddress
      .createUnresolved("127.0.0.1", 8383)
    response.underlying[play.shaded.ahc.org.asynchttpclient.Response].getRemoteAddress shouldBe InetSocketAddress
      .createUnresolved("127.0.0.1", 8384)

    ws.close()
  }

  test("multiple headers with same name should be retained & merged correctly") {
    val ws = MockWS { case (GET, "/get") =>
      Action { req =>
        Ok(req.headers.getAll("v1").zipWithIndex.toString)
      }
    }
    val request = ws
      .url("/get")
      .addHttpHeaders(("v1", "first"), ("v1", "second"))
      .get()

    val response = await(request)
    response.body shouldEqual "List((first,0), (second,1))"

    ws.close()
  }

  test("discard old headers when setting withHttpHeaders") {
    val headers = new AtomicReference[Map[String, scala.Seq[String]]](Map.empty)
    val ws = MockWS { case (GET, "/get") =>
      Action { req =>
        headers.set(req.headers.toMap)
        Ok(req.headers.getAll("key1").zipWithIndex.toString)
      }
    }
    val request = ws
      .url("/get")
      .withHttpHeaders("key1" -> "value1")
      .withHttpHeaders("key2" -> "value2")
      .get()

    await(request)
    val headersMap = headers.get()
    headersMap.get("key1") shouldBe None
    headersMap.get("key2") shouldBe Some(Seq("value2"))
  }

  test("discard old query parameters when setting withQueryStringParameters") {
    val queryString = new AtomicReference[Map[String, scala.Seq[String]]](Map.empty)
    val ws = MockWS { case (GET, "/get") =>
      Action { req =>
        queryString.set(req.queryString)
        Ok("")
      }
    }
    val request = ws
      .url("/get")
      .withQueryStringParameters("bar" -> "baz")
      .withQueryStringParameters("bar" -> "bah")
      .get()

    await(request)
    val queryMap = queryString.get()
    queryMap.get("bar") shouldBe Some(Seq("bah"))
  }

  test("keep headers with content type from BodyWritable") {
    val headers = new AtomicReference[Map[String, scala.Seq[String]]](Map.empty)
    val ws = MockWS { case (POST, "/") =>
      Action { req =>
        headers.set(req.headers.toMap)
        Ok
      }
    }
    val request = ws
      .url("/")
      .withHttpHeaders("key1" -> "value1")
      .post("test")

    val response = await(request)
    response.status shouldBe OK
    val headersMap = headers.get()
    headersMap.get("key1") shouldBe Some(Seq("value1"))
  }
}
