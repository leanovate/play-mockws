package mockws

import java.util.UUID

import scala.concurrent.Future
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import play.api.libs.ws.StandaloneWSClient
import play.api.libs.ws.StandaloneWSRequest
import play.api.mvc.EssentialAction
import play.api.mvc.Result
import play.api.mvc.Results.NotFound

/**
 * Mock implementation for the [[play.api.libs.ws.WSClient]].
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
class MockWS(routes: MockWS.Routes, shutdownHook: () => Unit)(
    implicit val materializer: Materializer,
    notFoundBehaviour: RouteNotDefined
) extends StandaloneWSClient {
  require(routes != null)

  override def underlying[T]: T = this.asInstanceOf[T]

  override def close(): Unit = {
    shutdownHook()
  }

  override def url(url: String): StandaloneWSRequest = FakeWSRequestHolder(routes, url)
}

object MockWS {
  type Routes = PartialFunction[(String, String), EssentialAction]

  /**
   * @param routes simulation of the external web resource
   */
  def apply(routes: Routes)(implicit notFoundBehaviour: RouteNotDefined) = {
    implicit val system = ActorSystem("mock-ws-" + UUID.randomUUID().toString)
    new MockWS(routes, () => system.terminate())
  }

  /**
   * @param routes       simulation of the external web resource
   * @param materializer user-defined materializer
   */
  def apply(routes: Routes, materializer: Materializer)(implicit notFoundBehaviour: RouteNotDefined) = {
    implicit val mat = materializer
    new MockWS(routes, () => ()) {}
  }

}

trait RouteNotDefined extends (() => Future[Result])
object RouteNotDefined {

  implicit val defaultAction: RouteNotDefined = RouteNotDefined(NotFound)

  def apply(result: Result): RouteNotDefined = new RouteNotDefined {
    override def apply(): Future[Result] = Future.successful(result)
  }
}
