package mockws

import java.io.ByteArrayInputStream
import java.net.URLEncoder

import com.ning.http.client.providers.netty.NettyResponse
import org.mockito.BDDMockito._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import play.api.http.{ContentTypeOf, Writeable}
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.json.Json
import play.api.libs.ws._
import play.api.mvc.EssentialAction
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.ClassTag
import scala.util.Try

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
 * MockWS.Routes is a partial function.
 * It is also possible to combine routes together:
 * {{{
 *   val index = MockWS.Routes {
 *     case ("GET", "/") => Action { Ok("index") }
 *   }
 *   val hiWorld = MockWS.Routes {
 *     case ("GET", "/hi") => Action { Ok("world") }
 *   }
 *   val ws = MockWS(index orElse hiWorld)
 * }}}
 *
 * @param withRoutes routes defining the mock calls
 */
case class MockWS(withRoutes: MockWS.Routes) extends WSClient {

  require(withRoutes != null)

  import mockws.MockWS.logger

  override def underlying[T]: T = this.asInstanceOf[T]

  private[this] val routes = (method: String, path: String) =>
    if (withRoutes.isDefinedAt(method, path))
      withRoutes.apply(method, path)
    else
      throw new Exception(s"no route defined for $method $path")

  def url(url: String): WSRequestHolder = {

    var method = GET
    val requestHeaders = mutable.Buffer[(String, String)]()
    val queryParameters = mutable.Map[String, String]()

    def urlWithQueryParam(u: String) = if (queryParameters.isEmpty) {
      u
    } else {
      val encode = (s: String) => URLEncoder.encode(s, "UTF-8")

      u + queryParameters
        .map { case (q: String, v: String) =>
          val encodedQ = encode(q)
          val encodedV = encode(v)
          s"$encodedQ=$encodedV"
        }
        .mkString("?", "&", "")
    }

    def buildResponse(method: String): Future[(WSResponseHeaders, Enumerator[Array[Byte]])] = {

      val _url = urlWithQueryParam(url)
      val action: EssentialAction = routes.apply(method, _url)
      logger.info(s"calling $method $url")
      val fakeRequest = FakeRequest(method, _url).withHeaders(requestHeaders: _*)
      val futureResult = action(fakeRequest).run
      futureResult map { result =>
        val wsResponseHeaders = new WSResponseHeaders {
          override val status: Int = result.header.status
          override val headers: Map[String, Seq[String]] = result.header.headers.mapValues(Seq(_))
        }
        (wsResponseHeaders, result.body)
      }
    }

    def answerStream(method: => String) = new Answer[Future[(WSResponseHeaders, Enumerator[Array[Byte]])]] {
      def answer(invocation: InvocationOnMock): Future[(WSResponseHeaders, Enumerator[Array[Byte]])] =
        buildResponse(method)
    }

    def answerIteratee(method: String) = new Answer[Future[Iteratee[Array[Byte], _]]] {
      def answer(invocation: InvocationOnMock): Future[Iteratee[Array[Byte], _]] = {
        val args = invocation.getArguments

        buildResponse(method) flatMap { case (wsResponseHeaders, stream) =>
          val consumer = args(0).asInstanceOf[WSResponseHeaders => Iteratee[Array[Byte], _]]
          val it = consumer.apply(wsResponseHeaders)
          stream.apply(it)
        }
      }
    }

    def wsRequestHolderAnswer(method: => String) = new Answer[Future[WSResponse]] {
      def answer(invocation: InvocationOnMock): Future[WSResponse] = {
        // request body
        val action: EssentialAction = routes.apply(method, url)

        val _url = urlWithQueryParam(url)
        val args = invocation.getArguments
        val futureResult = if (args.length == 3) {
          // ws was called with a body content. Extract this content and send it to the mock backend.
          val (bodyContent, mimeType) = extractBodyContent(args)
          logger.info(s"calling $method $url with '${new String(bodyContent)}' (mimeType:'$mimeType')")
          val requestBody = Enumerator(bodyContent) andThen Enumerator.eof
          val fakeRequest = mimeType match {
            case Some(m) => FakeRequest(method, _url).withHeaders(CONTENT_TYPE -> m).withHeaders(requestHeaders: _*)
            case None => FakeRequest(method, _url).withHeaders(requestHeaders: _*)
          }
          requestBody |>>> action(fakeRequest)
        } else {
          logger.info(s"calling $method $url")
          val fakeRequest = FakeRequest(method, _url).withHeaders(requestHeaders: _*)
          action(fakeRequest).run
        }

        for {
          result <- futureResult
          contentAsBytes <- result.body |>>> Iteratee.consume[Array[Byte]]()
        } yield {
          val wsResponse = mock(classOf[WSResponse])
          given (wsResponse.status) willReturn result.header.status
          given (wsResponse.header(any)) willAnswer mockHeaders(result.header.headers)
          val body = new String(contentAsBytes, charset(result.header.headers).getOrElse("utf-8"))
          given (wsResponse.body) willReturn body

          val returnedContentType = result.header.headers
            .get(CONTENT_TYPE)
            .flatMap { ct => Try(ct.split(";").take(1).mkString.trim).toOption }
            .map(_.toLowerCase)

          returnedContentType match {
            case Some("text/json") | Some("application/json") => given(wsResponse.json) willReturn Json.parse(body)
            case Some("text/xml") | Some("application/xml") => given(wsResponse.xml) willReturn scala.xml.XML.loadString(body)
            case _ => // wsResponse.body is already set
          }

          // underlying netty response
          val nettyResponse = mock(classOf[NettyResponse])
          given (wsResponse.underlying) willReturn nettyResponse
          given (nettyResponse.getResponseBodyAsStream) willReturn new ByteArrayInputStream(body.getBytes)

          wsResponse
        }
      }
    }

    val ws = mock(classOf[WSRequestHolder])
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
    given (ws.withQueryString(any)) will new Answer[WSRequestHolder] {
      override def answer(invocation: InvocationOnMock): WSRequestHolder = {
        for (arg <- invocation.getArguments) {
          queryParameters ++= arg.asInstanceOf[mutable.Seq[(String, String)]]
        }
        ws
      }
    }
    given (ws.withRequestTimeout(any)) willReturn ws
    given (ws.withVirtualHost(any)) willReturn ws
    
    given (ws.withMethod(any[String])) will new Answer[WSRequestHolder] {
      override def answer(invocation: InvocationOnMock): WSRequestHolder = {
        invocation.getArguments match {
          case Array(m: String) => method = m
        }
        ws
      }
    }
    
    given (ws.execute()) will wsRequestHolderAnswer(method)

    given (ws.get()) will wsRequestHolderAnswer(GET)
    given (ws.get(any)(any)) will answerIteratee(GET)
    given (ws.post(any[AnyRef])(any, any)) will wsRequestHolderAnswer(POST)
    given (ws.put(any[AnyRef])(any, any)) will wsRequestHolderAnswer(PUT)
    given (ws.delete()) will wsRequestHolderAnswer(DELETE)

    given (ws.stream()) will answerStream(method)
    given (ws.getStream()) will answerStream(GET)

    ws
  }

  private def any[T : ClassTag]: T = org.mockito.Matchers.any(implicitly[ClassTag[T]].runtimeClass).asInstanceOf[T]

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
  type Routes = PartialFunction[(String, String), EssentialAction]

  private val logger = play.api.Logger("mockws.MockWS")
}
