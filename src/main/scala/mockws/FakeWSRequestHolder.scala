package mockws

import java.net.URLEncoder

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Sink, Source}
import akka.util.ByteString
import mockws.MockWS.Routes
import org.slf4j.LoggerFactory
import play.api.libs.iteratee.Enumerator
import play.api.libs.streams.Streams
import play.api.libs.ws._
import play.api.libs.ws.ahc.AhcWSResponse
import play.api.mvc.Result
import play.api.test.FakeRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

case class FakeWSRequestHolder(
  routes: Routes,
  url: String,
  method: String = "GET",
  body: WSBody = EmptyBody,
  headers: Map[String, Seq[String]] = Map.empty,
  queryString: Map[String, Seq[String]] = Map.empty,
  requestTimeout: Option[Int] = None,
  timeoutProvider: TimeoutProvider = SchedulerExecutorServiceTimeoutProvider)(
  implicit val materializer: ActorMaterializer
) extends WSRequest {

  private val logger = LoggerFactory.getLogger(getClass)

  /* Not implemented. */
  override val auth: Option[(String, String, WSAuthScheme)] = None
  override val calc: Option[WSSignatureCalculator] = None
  override val followRedirects: Option[Boolean] = None
  override val proxyServer: Option[WSProxyServer] = None
  override val virtualHost: Option[String] = None

  def withAuth(username: String, password: String, scheme: WSAuthScheme) = this

  def sign(calc: WSSignatureCalculator): WSRequest = this

  def withFollowRedirects(follow: Boolean) = this

  def withProxyServer(proxyServer: WSProxyServer) = this

  def withVirtualHost(vh: String) = this

  def withBody(body: WSBody) = copy(body = body)

  def withMethod(method: String) = copy(method = method)

  def withHeaders(hdrs: (String, String)*) = {
    val headers = hdrs.foldLeft(this.headers)(
      (m, hdr) =>
        if (m.contains(hdr._1)) m.updated(hdr._1, m(hdr._1) :+ hdr._2)
        else m + (hdr._1 -> Seq(hdr._2))
    )
    copy(headers = headers)
  }

  def withQueryString(parameters: (String, String)*) = copy(
    queryString = parameters.foldLeft(queryString) {
      case (m, (k, v)) => m + (k -> (v +: m.getOrElse(k, Nil)))
    }
  )

  def withRequestTimeout(timeout: Duration): WSRequest =
    timeout match {
      case Duration.Inf =>
        copy(requestTimeout = None)
      case d =>
        val millis = d.toMillis
        require(millis >= 0 && millis <= Int.MaxValue, s"Request timeout must be between 0 and ${Int.MaxValue} milliseconds")
        copy(requestTimeout = Some(millis.toInt))
    }

  def withRequestFilter(filter: WSRequestFilter): WSRequest = this

  def execute(): Future[WSResponse] =
    for {
      result <- executeResult()
      responseBody ← result.body.dataStream.runFold(ByteString.empty)(_ ++ _)
    } yield new AhcWSResponse(new FakeAhcResponse(result, responseBody.toArray))


  def stream(): Future[StreamedResponse] =
    executeResult().map { result =>
      StreamedResponse(new FakeWSResponseHeaders(result), result.body.dataStream)
    }

  @deprecated("2.5.0")
  def streamWithEnumerator(): Future[(WSResponseHeaders, Enumerator[Array[Byte]])] =
    executeResult().map { r ⇒
      val headers = FakeWSResponseHeaders(r.header.status, r.header.headers.mapValues(e ⇒ Seq(e)))
      val source = r.body.dataStream.map(_.toArray)
      val publisher = source.runWith(Sink.asPublisher(false))
      val enum: Enumerator[Array[Byte]] = Streams.publisherToEnumerator(publisher)
      headers → enum
    }

  private def executeResult(): Future[Result] = {
    logger.debug(s"calling $method $url")

    val action = routes.lift((method, url)).getOrElse(throw new Exception(s"no route defined for $method $url"))
    def fakeRequest = FakeRequest(method, urlWithQueryParams()).withHeaders(headersSeq(): _*)

    // Real WSClients will actually interrupt the response Enumerator while it's streaming.
    // I don't want to go down that rabbit hole. This is close enough for most cases.
    applyRequestTimeout(fakeRequest) {
      action(fakeRequest).run(requestBodyEnumerator)
    }
  }

  private def applyRequestTimeout[T](req: FakeRequest[_])(future: Future[T]) = requestTimeout match {
    case Some(delay) => timeoutProvider.timeout(
      future = future,
      delay = delay.millis,
      timeoutMsg = s"Request ${req.method} ${req.uri} timed out after $delay ms."
    )
    case None => future
  }

  private def requestBodyEnumerator: Source[ByteString, _] = body match {
    case EmptyBody => Source.empty
    case FileBody(file) => FileIO.fromFile(file)
    case InMemoryBody(bytes) => Source.single(bytes)
    case StreamedBody(source) => source
  }

  private def headersSeq(): Seq[(String, String)] = for {
    (key, values) <- headers.toSeq
    value <- values
  } yield key -> value

  private def urlWithQueryParams() = if (queryString.isEmpty) {
    url
  } else {
    def encode(s: String): String = URLEncoder.encode(s, "UTF-8")

    url + queryString.flatMap { case (key: String, values: Seq[String]) =>
      values.map { value =>
        val encodedKey = encode(key)
        val encodedValue = encode(value)
        s"$encodedKey=$encodedValue"
      }
    }.mkString("?", "&", "")
  }
}
