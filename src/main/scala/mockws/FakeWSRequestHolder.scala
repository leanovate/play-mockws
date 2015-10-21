package mockws

import java.net.URLEncoder

import mockws.MockWS.Routes
import org.slf4j.LoggerFactory
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.ws._
import play.api.libs.ws.ning.NingWSResponse
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
  timeoutProvider: TimeoutProvider = SchedulerExecutorServiceTimeoutProvider
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

  def withRequestTimeout(timeout: Long) = copy(requestTimeout = Some(timeout.toInt))

  def execute(): Future[WSResponse] = for {
    result <- executeResult()
    responseBodyBytes <- result.body |>>> Iteratee.consume[Array[Byte]]()
  } yield new NingWSResponse(new FakeAhcResponse(result, responseBodyBytes))

  def stream(): Future[(WSResponseHeaders, Enumerator[Array[Byte]])] = {
    executeResult().map(result => (new FakeWSResponseHeaders(result), result.body))
  }

  private def executeResult(): Future[Result] = {
    logger.debug(s"calling $method $url")

    val action = routes.lift((method, url)).getOrElse(throw new Exception(s"no route defined for $method $url"))
    def fakeRequest = FakeRequest(method, urlWithQueryParams()).withHeaders(headersSeq(): _*)

    // Real WSClients will actually interrupt the response Enumerator while it's streaming.
    // I don't want to go down that rabbit hole. This is close enough for most cases.
    applyRequestTimeout(fakeRequest) {
      requestBodyEnumerator |>>> action(fakeRequest)
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

  private def requestBodyEnumerator: Enumerator[Array[Byte]] = body match {
    case EmptyBody => Enumerator.eof
    case FileBody(file) => Enumerator.fromFile(file)
    case InMemoryBody(bytes) => Enumerator(bytes)
    case StreamedBody(enumerator) => enumerator
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
