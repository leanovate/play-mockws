package mockws

import java.io.{ByteArrayInputStream, InputStream}
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util
import java.util.Collections

import com.ning.http.client.cookie.{Cookie, CookieDecoder}
import com.ning.http.client.uri.Uri
import com.ning.http.client.{FluentCaseInsensitiveStringsMap, Response}
import com.ning.http.util.AsyncHttpProviderUtils
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import play.api.mvc.Result

import scala.collection.JavaConversions._

/**
 * A simulated response from the async-http-client.
 *
 * The [[play.api.libs.ws.ning.NingWSResponse]] is intended to wrap this.
 *
 * Implementation is mostly based off of [[com.ning.http.client.providers.netty.response.NettyResponse]].
 *
 * We're faking at this level as opposed to the [[play.api.libs.ws.WSResponse]] level
 * to preserve any behavior specific to the NingWSResponse which is likely to be used
 * in the real (non-fake) WSClient.
 */
class FakeAhcResponse(result: Result, body: Array[Byte]) extends Response {

  private val NettyDefaultCharset: Charset = Charset.forName("ISO-8859-1")

  override def getResponseBodyAsByteBuffer: ByteBuffer = ByteBuffer.wrap(body)

  override def getResponseBodyExcerpt(maxLength: Int, charset: String): String = getResponseBody(charset).take(maxLength)

  override def getResponseBodyExcerpt(maxLength: Int): String = getResponseBodyExcerpt(maxLength, null)

  override def getStatusCode: Int = result.header.status

  override def getResponseBodyAsBytes: Array[Byte] = body

  override def getResponseBodyAsStream: InputStream = new ByteArrayInputStream(body)

  override def isRedirected: Boolean = Set(301, 302, 303, 307, 308).contains(getStatusCode)

  override def getCookies: util.List[Cookie] = getHeaders("Set-Cookie").map(CookieDecoder.decode)

  override def hasResponseBody: Boolean = body.nonEmpty

  override def getStatusText: String = HttpResponseStatus.valueOf(getStatusCode).toString

  override def getHeaders(name: String): util.List[String] = {
    Option(getHeaders.get(name)).getOrElse(Collections.emptyList())
  }

  override def getHeaders: FluentCaseInsensitiveStringsMap = {
    val scalaHeaders = FakeWSResponseHeaders.toMultiMap(result.header)
    val javaHeaders = mapAsJavaMap(scalaHeaders.mapValues(v => asJavaCollection(v)))

    new FluentCaseInsensitiveStringsMap(javaHeaders)
  }

  override def hasResponseHeaders: Boolean = true // really asking if the request has been completed.

  override def getResponseBody(charset: String): String = new String(body, computeCharset(charset))

  override def getResponseBody: String = getResponseBody(null)

  override def getContentType: String = getHeader("Content-Type")

  override def hasResponseStatus: Boolean = true // really asking if the request has been completed.

  override def getUri: Uri = throw new NotImplementedError("unavailable here and unused by NingWSResponse")

  override def getHeader(name: String): String = getHeaders.getFirstValue(name)

  private def computeCharset(charset: String): Charset = {
    Option(charset)
      .orElse(charsetFromContentType)
      .map(Charset.forName)
      .getOrElse(NettyDefaultCharset)
  }

  private def charsetFromContentType: Option[String] = {
    Option(getContentType)
      .flatMap(ct => Option(AsyncHttpProviderUtils.parseCharset(ct)))
  }
}
