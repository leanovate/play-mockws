package mockws

import play.api.libs.ws.{WSClient, WSRequest}
import play.api.mvc.EssentialAction

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
 * @param routes routes defining the mock calls
 */
class MockWS(routes: MockWS.Routes) extends WSClient {
  require(routes != null)

  override def underlying[T]: T = this.asInstanceOf[T]

  override def close(): Unit = {}

  override def url(url: String): WSRequest = new FakeWSRequestHolder(routes, url)
}

object MockWS {
  type Routes = PartialFunction[(String, String), EssentialAction]

  def apply(routes: Routes) = new MockWS(routes)
}
