package mockws

import java.io.ByteArrayInputStream

import com.ning.http.client.providers.netty.NettyResponse
import org.mockito.BDDMockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.specs2.mock.Mockito
import play.api.Logger
import play.api.http.{ContentTypeOf, Writeable}
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSRequestHolder, WSResponse, WSResponseHeaders}
import play.api.mvc.EssentialAction
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

/**
 * Mock implementation for the [[play.api.libs.ws.WS]] client.
 * Usage:
 * {{{
 *   val ws = MockWS {
 *     case ("GET", "/") => Action { Ok("index") }
 *     case ("GET", "/hi") => Action { Ok("world") }
 *   }
 * }}}
 *
 * MockWS.Route is a partial function.
 * It is also possible to combine routes together:
 * {{{
 *   val index = MockWS.Route {
 *     case ("GET", "/") => Action { Ok("index") }
 *   }
 *   val hiWorld = MockWS.Route {
 *     case ("GET", "/hi") => Action { Ok("world") }
 *   }
 *   val ws = MockWS(index orElse hiWorld)
 * }}}
 *
 * @param withRoutes routes defining the mock calls
 */
case class MockWS(withRoutes: MockWS.Route) extends WSClient with Mockito {

  require(withRoutes != null)

  override def underlying[T]: T = this.asInstanceOf[T]

  private[this] val routes = (method: String, path: String) =>
    if (withRoutes.isDefinedAt(method, path))
      withRoutes.apply(method, path)
    else
      throw new Exception(s"no route defined for $method $path")

  def url(url: String): WSRequestHolder = {

    val requestHeaders = mutable.Buffer[(String, String)]()

    def answerStream(method: String) = new Answer[Future[(WSResponseHeaders, Enumerator[Array[Byte]])]] {
      def answer(invocation: InvocationOnMock): Future[(WSResponseHeaders, Enumerator[Array[Byte]])] = {

        val action: EssentialAction = routes.apply(method, url)
        Logger.info(s"calling $method $url")
        val fakeRequest = FakeRequest(method, url).withHeaders(requestHeaders: _*)
        val futureResult = action(fakeRequest).run
        futureResult map { result =>
          val wsResponseHeaders = new WSResponseHeaders {
            override val status: Int = result.header.status
            override val headers: Map[String, Seq[String]] = result.header.headers.mapValues(Seq(_))
          }
          (wsResponseHeaders, result.body)
        }
      }
    }

    def wsRequestHolderAnswer(method: String) = new Answer[Future[WSResponse]] {
      def answer(invocation: InvocationOnMock): Future[WSResponse] = {
        // request body
        val action: EssentialAction = routes.apply(method, url)

        val args = invocation.getArguments
        val futureResult = if (args.length == 3) {
          // ws was called with a body content. Extract this content and send it to the mock backend.
          val (bodyContent, mimeType) = extractBodyContent(args)
          Logger.info(s"calling $method $url with '${new String(bodyContent)}' (mimeType:'$mimeType')")
          val requestBody = Enumerator(bodyContent) andThen Enumerator.eof
          val fakeRequest = mimeType match {
            case Some(m) => FakeRequest(method, url).withHeaders(CONTENT_TYPE -> m).withHeaders(requestHeaders: _*)
            case None => FakeRequest(method, url).withHeaders(requestHeaders: _*)
          }
          requestBody |>>> action(fakeRequest)
        } else {
          Logger.info(s"calling $method $url")
          val fakeRequest = FakeRequest(method, url).withHeaders(requestHeaders: _*)
          action(fakeRequest).run
        }

        futureResult map { result =>
          val wsResponse = mock[WSResponse]
          given (wsResponse.status) willReturn result.header.status
          given (wsResponse.header(any)) willAnswer mockHeaders(result.header.headers)
          val contentAsBytes: Array[Byte] = Await.result(result.body |>>> Iteratee.consume[Array[Byte]](), 5 seconds)
          val body = new String(contentAsBytes, charset(result.header.headers).getOrElse("utf-8"))
          given (wsResponse.body) willReturn body
          val returnedContentType = result.header.headers.get(CONTENT_TYPE).map(_.split(";").take(1).mkString.trim)
          returnedContentType match {
            case Some("application/json") => given(wsResponse.json) willReturn Json.parse(body)
            case Some("text/xml") => given(wsResponse.xml) willReturn scala.xml.XML.loadString(body)
            case Some("application/xml") => given(wsResponse.xml) willReturn scala.xml.XML.loadString(body)
            case Some("text/html") => throw new Exception(s"[$method $url]: receive html '$body'")
            case Some("text/plain") | Some("application/octet-stream") | None => // wsResponse.body is already set
            case Some(t) => throw new Exception(s"[$method $url]: cannot parse content type '$t'")
          }

          // underlying netty response
          val nettyResponse = mock[NettyResponse]
          given (wsResponse.underlying) willReturn nettyResponse
          given (nettyResponse.getResponseBodyAsStream) willReturn new ByteArrayInputStream(body.getBytes)

          wsResponse
        }
      }
    }

    val ws = mock[WSRequestHolder]
    given (ws.withAuth(any, any, any)) willReturn ws
    given (ws.withFollowRedirects(any)) willReturn ws
    given (ws.withHeaders(any)) will new Answer[WSRequestHolder] {
      override def answer(invocation: InvocationOnMock): WSRequestHolder = {
        for (arg <- invocation.getArguments) {
          requestHeaders ++= arg.asInstanceOf[mutable.Seq[(String, String)]]
        }
        ws
      }
    }
    given (ws.withQueryString(any)) willReturn ws
    given (ws.withRequestTimeout(any)) willReturn ws
    given (ws.withVirtualHost(any)) willReturn ws

    given (ws.get()) will wsRequestHolderAnswer(GET)
    given (ws.post(any[AnyRef])(any, any)) will wsRequestHolderAnswer(POST)
    given (ws.put(any[AnyRef])(any, any)) will wsRequestHolderAnswer(PUT)
    given (ws.delete()) will wsRequestHolderAnswer(DELETE)

    given (ws.stream()) will answerStream(GET)
    given (ws.getStream()) will answerStream(GET)

    ws
  }

  private[this] def extractBodyContent[T](args: Array[Object]): (Array[Byte], Option[String]) = {
    val bodyObject = args(0).asInstanceOf[T]
    val writeable = args(1).asInstanceOf[Writeable[T]]
    val contentTypeOf = args(2).asInstanceOf[ContentTypeOf[T]]
    val mimeType = contentTypeOf.mimeType
    (writeable.transform(bodyObject), mimeType)
  }

  private[this] def mockHeaders(headers: Map[String, String]) = new Answer[Option[String]] {
    def answer(invocation: InvocationOnMock): Option[String] = {
      val args = invocation.getArguments
      val key = args(0).asInstanceOf[String]
      headers.get(key)
    }
  }

  private[this] def charset(headers: Map[String, String]): Option[String] = headers.get(CONTENT_TYPE) match {
    case Some(s) if s.contains("charset=") => Some(s.split("; charset=").drop(1).mkString.trim)
    case _ => None
  }

}

object MockWS {
  type Route = PartialFunction[(String, String), EssentialAction]
}
