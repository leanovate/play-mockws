package mockws

import java.io.File
import java.net.{URI, URLEncoder}
import java.util.Base64

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import mockws.MockWS.Routes
import org.slf4j.LoggerFactory
import play.api.libs.ws._
import play.api.libs.ws.ahc.AhcWSResponse
import play.api.mvc.MultipartFormData.Part
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.core.formatters.Multipart
import play.shaded.ahc.io.netty.handler.codec.http.HttpHeaders

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
  auth: Option[(String, String, WSAuthScheme)] = None,
  requestTimeout: Option[Int] = None,
  timeoutProvider: TimeoutProvider = SchedulerExecutorServiceTimeoutProvider)(
  implicit val materializer: ActorMaterializer
) extends WSRequest {

  override type Self = WSRequest
  override type Response = WSResponse

  private val logger = LoggerFactory.getLogger(getClass)

  /* Not implemented. */
  val calc: Option[WSSignatureCalculator] = None
  val followRedirects: Option[Boolean] = None
  val proxyServer: Option[WSProxyServer] = None
  val virtualHost: Option[String] = None

  def withAuth(username: String, password: String, scheme: WSAuthScheme): Self =
    copy(auth = Some((username, password, scheme)))

  def sign(calc: WSSignatureCalculator): Self = this

  def withFollowRedirects(follow: Boolean): Self = this

  def withProxyServer(proxyServer: WSProxyServer): Self = this

  def withVirtualHost(vh: String): Self = this

  def withBody(body: WSBody): Self = copy(body = body)

  def withMethod(method: String): Self = copy(method = method)

  def withHeaders(hdrs: (String, String)*): Self = {
    val headers = hdrs.foldLeft(this.headers)(
      (m, hdr) =>
        if (m.contains(hdr._1)) m.updated(hdr._1, m(hdr._1) :+ hdr._2)
        else m + (hdr._1 -> Seq(hdr._2))
    )
    copy(headers = headers)
  }

  def withQueryString(parameters: (String, String)*): Self = copy(
    queryString = parameters.foldLeft(queryString) {
      case (m, (k, v)) => m + (k -> (v +: m.getOrElse(k, Nil)))
    }
  )

  def withRequestTimeout(timeout: Duration): Self =
    timeout match {
      case Duration.Inf =>
        copy(requestTimeout = None)
      case d =>
        val millis = d.toMillis
        require(millis >= 0 && millis <= Int.MaxValue, s"Request timeout must be between 0 and ${Int.MaxValue} milliseconds")
        copy(requestTimeout = Some(millis.toInt))
    }

  def withRequestFilter(filter: WSRequestFilter): Self = this

  def execute(): Future[Response] =
    for {
      result <- executeResult()
      responseBody ← result.body.dataStream.runFold(ByteString.empty)(_ ++ _)
    } yield new AhcWSResponse(new FakeAhcResponse(result, responseBody.toArray))


  def stream(): Future[StreamedResponse] =
    executeResult().map { result =>
      StreamedResponse(new FakeWSResponseHeaders(result), result.body.dataStream)
    }

  private def executeResult(): Future[Result] = {
    logger.debug(s"calling $method $url")

    val action = routes.lift((method, url)).getOrElse(throw new Exception(s"no route defined for $method $url"))
    def fakeRequest = FakeRequest(method, urlWithQueryParams()).withHeaders(headersSeq(): _*).withBody(body)

    // Real WSClients will actually interrupt the response Enumerator while it's streaming.
    // I don't want to go down that rabbit hole. This is close enough for most cases.
    applyRequestTimeout(fakeRequest) {
      action(sign(fakeRequest)).run(requestBodySource)
    }
  }

  private def sign(req: FakeRequest[_]): FakeRequest[_] = auth match {
    case None ⇒ req
    case Some((username, password, WSAuthScheme.BASIC)) ⇒
      val encoded = new String(
        Base64.getMimeEncoder().encode(s"$username:$password".getBytes("UTF-8")), "UTF-8")
      req.withHeaders("Authorization" → s"Basic: $encoded")
    case Some((_, _, unsupported)) ⇒ throw new UnsupportedOperationException(
      s"""do not support auth method $unsupported.
        |Help us to provide support for this.
        |Send us a test at https://github.com/leanovate/play-mockws/issues
      """.stripMargin)
  }

  private def applyRequestTimeout[T](req: FakeRequest[_])(future: Future[T]) = requestTimeout match {
    case Some(delay) => timeoutProvider.timeout(
      future = future,
      delay = delay.millis,
      timeoutMsg = s"Request ${req.method} ${req.uri} timed out after $delay ms."
    )
    case None => future
  }

  private def requestBodySource: Source[ByteString, _] = body match {
    case EmptyBody => Source.empty
    case FileBody(file) => FileIO.fromPath(file.toPath)
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

  override def withBody(body: Source[Part[Source[ByteString, _]], _]): Self = {
    val boundary = Multipart.randomBoundary()
    val contentType = s"multipart/form-data; boundary=$boundary"
    withBody(StreamedBody(Multipart.transform(body, boundary))).withHeaders("Content-Type" -> contentType)
  }

  override def patch(body: Source[Part[Source[ByteString, _]], _]): Future[Response] =
    withBody(body).execute("PATCH")

  override def post(body: Source[Part[Source[ByteString, _]], _]): Future[Response] =
    withBody(body).execute("POST")

  override def put(body: Source[Part[Source[ByteString, _]], _]): Future[Response] =
    withBody(body).execute("PUT")

  override def get(): Future[Response] = execute("GET")

  override def contentType: Option[String] =
    headers.get(HttpHeaders.Names.CONTENT_TYPE).flatMap(_.headOption)

  override def delete(): Future[Response] = execute("DELETE")

  override def execute(method: String): Future[Response] = withMethod(method).execute()

  override def head(): Future[Response] = execute("HEAD")

  override def options(): Future[Response] = execute("OPTIONS")

  override def patch(body: File): Future[Response] =
    withBody(body).execute("PATCH")

  override def patch[T](body: T)(implicit ev: BodyWritable[T]): Future[Response] =
    withBody(body).execute("PATCH")

  override def post(body: File): Future[Response] =
    withBody(body).execute("POST")

  override def post[T](body: T)(implicit ev: BodyWritable[T]): Future[Response] =
    withBody(body).execute("POST")

  override def put(body: File): Future[Response] =
    withBody(body).execute("PUT")

  override def put[T](body: T)(implicit ev: BodyWritable[T]): Future[Response] =
    withBody(body).execute("PUT")

  override def withBody[T](body: T)(implicit ev: BodyWritable[T]): Self =
    withBodyAndContentType(InMemoryBody(ev.transform(body)), ev.contentType)

  override def withBody(file: java.io.File): Self = copy(body = FileBody(file))

  lazy val uri: URI = {
    val enc = (p: String) => java.net.URLEncoder.encode(p, "utf-8")
    new java.net.URI(if (queryString.isEmpty) url else {
      val qs = (for {
        (n, vs) <- queryString
        v <- vs
      } yield s"${enc(n)}=${enc(v)}").mkString("&")
      s"$url?$qs"
    })
  }

  private def withBodyAndContentType(wsBody: WSBody, contentType: String): Self = {
    if (headers.contains("Content-Type")) {
      withBody(wsBody)
    } else {
      withBody(wsBody).withHeaders("Content-Type" -> contentType)
    }
  }
}
