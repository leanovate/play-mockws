package mockws

import java.io._
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

import play.shaded.ahc.org.asynchttpclient.Response
import mockws.MockWSHelpers._
import play.api.mvc.Results._
import play.api.test.Helpers._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class GzippedResponsesTest extends AnyFunSuite with Matchers {

  test("mock WS handle gzipped responses") {
    val ws = MockWS {
      case (_, _) =>
        Action {
          val os   = new ByteArrayOutputStream()
          val gzip = new GZIPOutputStream(os)
          gzip.write("my response".getBytes())
          gzip.close()

          Ok(os.toByteArray)
        }
    }

    val result = await(ws.url("").get())

    val body =
      scala.io.Source.fromInputStream(new GZIPInputStream(result.underlying[Response].getResponseBodyAsStream)).mkString
    body shouldEqual "my response"
    ws.close()
  }

}
