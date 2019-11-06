package mockws

import play.api.mvc.ResponseHeader
import play.api.mvc.Result

case class FakeWSResponseHeaders(status: Int, headers: Map[String, Seq[String]]) {

  def this(result: Result) = this(result.header.status, FakeWSResponseHeaders.toMultiMap(result.header))
}

object FakeWSResponseHeaders {

  /**
   * Transition the response headers from {{{Map[String, String]}}} to {{{Map[String, Seq[String]]}}}.
   *
   * The closest behavior to the real WSClient implementations is wrap each value in a Seq() of size 1.
   *
   * Justification:
   * Multiple header response values for a single key are legal per RFC 2616 4.2 in two forms.
   *
   * 1. Comma separated (e.g. Cache-Control: no-store, no-cache)
   * NingWSClient represents these as Map("Cache-Control" -> Seq("no-store, no-cache"))
   * rather than splitting them, so we shall too.
   *
   * 2. Split into header key-value pairs (e.g. Cache-Control: no-store, Cache-Control: no-cache)
   * Play 2.3 has a bug and is incapable of producing this response, so we don't have to worry about it.
   * TL;DR: play.api.mvc.ResponseHeader uses a Map[String, String] which doesn't allow key collisions.
   *
   * See: https://github.com/playframework/playframework/issues/3544
   */
  def toMultiMap(header: ResponseHeader): Map[String, Seq[String]] = header.headers.map { case (k, v) => (k, Seq(v)) }
}
