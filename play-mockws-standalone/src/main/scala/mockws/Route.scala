package mockws

import java.util.concurrent.atomic.AtomicInteger

import play.api.mvc.EssentialAction

/**
 * traces whether a route was called and how many times.
 * Usage:
 * {{{
 *  val route1 = Route {
 *    case (GET, "/route1") => Action { Ok("") }
 *  }
 *  val route2 = Route {
 *    case (GET, "/route2") => Action { Ok("") }
 *  }
 *
 *  val ws = MockWS(route1 orElse route2)
 *
 *  await(ws.url("/route1").get())
 *
 *  route1.called == true
 *  route2.called == false
 * }}}
 */
case class Route(route: MockWS.Routes) extends MockWS.Routes {

  private val _timeCalled = new AtomicInteger(0)

  override def isDefinedAt(x: (String, String)): Boolean = route.isDefinedAt(x)

  override def apply(r: (String, String)): EssentialAction = {
    _timeCalled.getAndIncrement
    route.apply(r)
  }

  /**
   * @return true if the route was called at least one time
   */
  def called: Boolean = _timeCalled.get() > 0

  /**
   * @return the number of times the route was called
   */
  def timeCalled: Int = _timeCalled.get()
}
