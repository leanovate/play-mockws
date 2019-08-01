package mockws

import mockws.MockWSHelpers._
import org.scalatest.FunSuite
import org.scalatest.Matchers
import play.api.mvc.Results._
import play.api.test.Helpers._

/**
 * Tests RFC-2616 4.2:
 *
 * {{{
 * Multiple message-header fields with the same field-name MAY be present in a
 * message if and only if the entire field-value for that header field is
 * defined as a comma-separated list [i.e., #(values)].
 *
 * It MUST be possible to combine the multiple header fields into one
 * "field-name: field-value" pair, without changing the semantics of the message,
 * by appending each subsequent field-value to the first, each separated by a comma.
 *
 * The order in which header fields with the same field-name are received is therefore
 * significant to the interpretation of the combined field value, and thus a proxy
 * MUST NOT change the order of these field values when a message is forwarded.
 * }}}
 *
 * Due to a bug in play, we can't fully support this. https://github.com/playframework/playframework/issues/3544
 */
class ResponseHeaderTest extends FunSuite with Matchers {

  test("Multiple response headers with comma separated values should be returned unmodified") {
    val ws = MockWS {
      case (_, _) => Action(NoContent.withHeaders("Cache-Control" -> "no-cache, no-store"))
    }

    val headerValues = await(ws.url("/").get()).headers("Cache-Control")
    headerValues should have size 1
    headerValues.head shouldBe "no-cache, no-store"
    ws.close()
  }
}
