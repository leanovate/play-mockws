package mockws

import org.scalatest.{FunSuite, Matchers}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.Concurrent._
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponseHeaders}
import play.api.mvc.BodyParsers.parse
import play.api.mvc.Results._
import play.api.mvc.{Action, ResponseHeader, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Promise

/**
 * Tests that [[MockWS]] simulates a WS client
 */
class MockWSTest extends FunSuite with Matchers {

  test("mock WS simulates all HTTP methods") {
    val ws = MockWS {
      case (GET, "/get")       => Action { Ok("get ok") }
      case (POST, "/post")     => Action { Ok("post ok") }
      case (PUT, "/put")       => Action { Ok("put ok") }
      case (DELETE, "/delete") => Action { Ok("delete ok") }
    }

    await(ws.url("/get").get()).body       shouldEqual "get ok"
    await(ws.url("/post").post("")).body   shouldEqual "post ok"
    await(ws.url("/put").put("")).body     shouldEqual "put ok"
    await(ws.url("/delete").delete()).body shouldEqual "delete ok"
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
  }


  test("mock WS simulates a POST with a JSON payload") {
    val ws = MockWS {
      case (POST, "/") => Action { request =>
        Ok((request.body.asJson.get \ "result").as[String])
      }
    }

    val json = Json.parse("""{"result": "OK"}""")

    val response = await(ws.url("/").post(json))
    response.status shouldEqual OK
    response.body shouldEqual "OK"
  }


  test("mock WS simulates a POST with a JSON payload with a custom content type") {
    val ws = MockWS {
      case (POST, "/") => Action(parse.tolerantJson) { request =>
        Ok((request.body \ "result").as[String])
      }
    }

    val json = Json.parse("""{"result": "OK"}""")

    val response = await(ws.url("/").withHeaders(CONTENT_TYPE -> "application/my-json").post(json))
    response.status shouldEqual OK
    response.body shouldEqual "OK"
  }


  test("mock WS sets the response content type") {
    val ws = MockWS {
      case (GET, "/text") => Action { Ok("text") }
      case (GET, "/json") => Action { Ok(Json.parse("""{ "type": "json" }""")) }
    }

    val text = await(ws.url("/text").get())
    val json = await(ws.url("/json").get())

    text.header(CONTENT_TYPE) shouldEqual Some("text/plain; charset=utf-8")
    json.header(CONTENT_TYPE) shouldEqual Some("application/json; charset=utf-8")
  }


  test("mock WS simulates a streaming") {

    def testedController(ws: WSClient) = Action.async {
      ws.url("/").stream().map { case (rh, content) =>
        Result(
          header = ResponseHeader(rh.status, rh.headers.mapValues(_.head)),
          body = content
        )
      }
    }

    val ws = MockWS {
      case (GET, "/") => Action {
        Result(
          header = ResponseHeader(201, Map("x-header" -> "x-value")),
          body = Enumerator("first", "second", "third").map(_.getBytes)
        )
      }
    }

    val response = testedController(ws).apply(FakeRequest())
    status(response) shouldEqual CREATED
    contentAsString(response) shouldEqual "firstsecondthird"
    header("x-header", response) shouldEqual Some("x-value")
  }


  test("mock WS simulates a GET with a consumer") {

    def testedController(ws: WSClient) = Action.async {
      val resultP = Promise[Result]()
      def consumer(rh: WSResponseHeaders): Iteratee[Array[Byte], Unit] = {
        val (wsConsumer, content) = joined[Array[Byte]]
        resultP.success(Result(
          header = ResponseHeader(rh.status, rh.headers.mapValues(_.head)),
          body = content
        ))
        wsConsumer
      }

      ws.url("/").get(consumer).map(_.run)
      resultP.future
    }

    val ws = MockWS {
      case (GET, "/") => Action {
        Result(
          header = ResponseHeader(201, Map("x-header" -> "x-value")),
          body = Enumerator("first", "second", "third").map(_.getBytes)
        )
      }
    }

    val response = testedController(ws).apply(FakeRequest())
    status(response) shouldEqual CREATED
    contentAsString(response) shouldEqual "firstsecondthird"
    header("x-header", response) shouldEqual Some("x-value")
  }


  test("mock WS can produce JSON") {
    val ws = MockWS {
      case (GET, "/json") => Action {
        Ok(Json.obj("field" -> "value"))
      }
    }

    val wsResponse = await( ws.url("/json").get() )
    wsResponse.body shouldEqual """{"field":"value"}"""
    (wsResponse.json \ "field").asOpt[String] shouldEqual Some("value")
  }


  test("mock WS can produce XML") {
    val ws = MockWS {
      case (GET, "/xml") => Action {
        Ok(<foo><bar>value</bar></foo>)
      }
    }

    val wsResponse = await( ws.url("/xml").get() )
    wsResponse.body shouldEqual "<foo><bar>value</bar></foo>"
    (wsResponse.xml \ "bar").text shouldEqual "value"
  }


  test("a call to an unknown route causes an exception") {
    val ws = MockWS {
      case (GET, "/url") => Action { Ok("") }
    }

    the [Exception] thrownBy {
      ws.url("/url2").get()
    } should have message "no route defined for GET /url2"

    the [Exception] thrownBy {
      ws.url("/url").delete()
    } should have message "no route defined for DELETE /url"
  }

  test("mock WS supports custom response content types") {
    val ws = MockWS {
      case (_, _) => Action {
        Ok("hello").as("hello/world")
      }
    }

    val wsResponse = await( ws.url("/").get() )
    wsResponse.status shouldEqual OK
    wsResponse.header(CONTENT_TYPE) shouldEqual Some("hello/world")
    wsResponse.body shouldEqual "hello"
  }

  test("mock WS supports custom request content types") {
    val ws = MockWS {
      case (_, _) => Action { request =>
        request.contentType match {
          case Some(ct) => Ok(ct)
          case None => BadRequest("no content type")
        }
      }
    }

    val wsResponse = await(ws.url("/").withHeaders(CONTENT_TYPE -> "hello/world").get)
    wsResponse.status shouldEqual OK
    wsResponse.body shouldEqual "hello/world"
  }

  test("mock WS supports adding query parameters via the withQueryString method") {
    val ws = MockWS {
      case (GET, "/url?a=1&b=2&c=3") => Action {
        Ok("success")
      }
    }

    val requestHolder = ws.url("/url")
      .withQueryString("a" -> "1", "b" -> "2")
      .withQueryString("c" -> "3")
    val wsResponse = await(requestHolder.get())
    wsResponse.status shouldEqual OK
    wsResponse.body shouldEqual "success"
  }

  test("mock WS does not add question mark to url when withQueryString is called with empyu sequence") {
    val ws = MockWS {
      case (GET, "/url") => Action {
        Ok("success")
      }
    }

    val requestHolder = ws.url("/url")
      .withQueryString(Seq(): _*)
    val wsResponse = await(requestHolder.get())
    wsResponse.status shouldEqual OK
    wsResponse.body shouldEqual "success"
  }
}
