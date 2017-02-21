package mockws

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
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
class MockWS(routes: MockWS.Routes, shutdownHook: () ⇒ Unit)(implicit val materializer: ActorMaterializer) extends WSClient {
  require(routes != null)

  override def underlying[T]: T = this.asInstanceOf[T]

  override def close(): Unit = {
    shutdownHook()
  }

  override def url(url: String): WSRequest = FakeWSRequestHolder(routes, url)
}

object MockWS {
  type Routes = PartialFunction[(String, String), EssentialAction]

  /**
    * @param routes simulation of the external web resource
    */
  def apply(routes: Routes) = {
    implicit val system = ActorSystem("mock-ws-" + UUID.randomUUID().toString)
    implicit val materializer = ActorMaterializer()
    new MockWS(routes, () ⇒ system.terminate())
  }

  /**
    * @param routes       simulation of the external web resource
    * @param materializer user-defined materializer
    */
  def apply(routes: Routes, materializer: ActorMaterializer) = new MockWS(routes, () ⇒ Unit)(materializer)
}
