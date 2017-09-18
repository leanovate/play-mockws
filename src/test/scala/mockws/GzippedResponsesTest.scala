package mockws

import java.io._
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

import play.shaded.ahc.org.asynchttpclient.Response
import mockws.MockWSHelpers._
import org.scalatest.{FunSuite, Matchers}
import play.api.mvc.Results._
import play.api.test.Helpers._

class GzippedResponsesTest extends FunSuite with Matchers {

  test("mock WS handle gzipped responses") {
    val ws = MockWS {
      case (_, _) ⇒ Action {
        val os = new ByteArrayOutputStream()
        val gzip = new GZIPOutputStream(os)
        gzip.write("my response".getBytes())
        gzip.close()

        Ok(os.toByteArray)
      }
    }

    val result = await(ws.url("").get())

    val body = scala.io.Source.fromInputStream(new GZIPInputStream(result.underlying[Response].getResponseBodyAsStream)).mkString
    body shouldEqual "my response"
    ws.close()
  }

}
