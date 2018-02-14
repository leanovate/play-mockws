package mockws

import mockws.MockWSHelpers._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FunSuite, Matchers}
import play.api.libs.ws.WSAuthScheme
import play.api.mvc.Results._
import play.api.test.Helpers._

/**
 * Tests that [[MockWS]] simulates a WS client, in particular the methods involving authentication
 */
class AuthenticationTest extends FunSuite with Matchers with PropertyChecks {

  test("mock WS supports authentication with Basic Auth") {

    val BasicAuth = "Basic ((?:[A-Za-z0-9+/]{4})+(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?)".r

    val ws = MockWS {
      case (_, _) => Action { request =>
        request.headers.get(AUTHORIZATION) match {
          case Some(BasicAuth("dXNlcjpzM2NyM3Q=")) => Ok
          case _ => Unauthorized
        }
      }
    }

    val wsResponseOk = await(ws.url("/").withAuth("user", "s3cr3t", WSAuthScheme.BASIC).get)
    wsResponseOk.status shouldEqual OK

    val wsResponseUnauthorized = await(ws.url("/").withAuth("user", "secret", WSAuthScheme.BASIC).get)
    wsResponseUnauthorized.status shouldEqual UNAUTHORIZED

    ws.close()

  }


  test("mock WS does not support authentication with `WSAuthScheme.{NTLM, DIGEST, KERBEROS, SPNEGO}`") {

    val ws = MockWS { case (_, _) => Action { request => Ok } }

    a [UnsupportedOperationException] shouldBe thrownBy (await(ws.url("/").withAuth("user", "s3cr3t", WSAuthScheme.NTLM).get))
    a [UnsupportedOperationException] shouldBe thrownBy (await(ws.url("/").withAuth("user", "s3cr3t", WSAuthScheme.DIGEST).get))
    a [UnsupportedOperationException] shouldBe thrownBy (await(ws.url("/").withAuth("user", "s3cr3t", WSAuthScheme.KERBEROS).get))
    a [UnsupportedOperationException] shouldBe thrownBy (await(ws.url("/").withAuth("user", "s3cr3t", WSAuthScheme.SPNEGO).get))

  }

}
