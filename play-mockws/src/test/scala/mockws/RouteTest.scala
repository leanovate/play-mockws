package mockws

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import mockws.MockWSHelpers._
import play.api.mvc.Results._
import play.api.test.Helpers._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

/**
 * Tests [[Route]]
 */
class RouteTest extends AnyFunSuite with Matchers {

  test("a route knows if it was called") {
    val route1 = Route { case (GET, "/route1") =>
      Action { Ok("") }
    }
    val route2 = Route { case (GET, "/route2") =>
      Action { Ok("") }
    }

    val ws = MockWS(route1.orElse(route2))

    await(ws.url("/route1").get())

    route1.called shouldEqual true
    route2.called shouldEqual false

    await(ws.url("/route2").get())

    route1.called shouldEqual true
    route2.called shouldEqual true
    ws.close()
  }

  test("a route knows how many times it was called") {
    val route = Route { case (GET, "/route") =>
      Action { Ok("") }
    }
    val ws = MockWS(route)

    route.timeCalled shouldEqual 0

    await(ws.url("/route").get())

    route.timeCalled shouldEqual 1

    await(ws.url("/route").get())

    route.timeCalled shouldEqual 2

    ws.close()
  }

  test("a route knows how many times it was called with parallel calls") {
    val route = Route { case (GET, "/route") =>
      Action { Ok("") }
    }
    val ws = MockWS(route)

    val numberOfTimes = 20

    val executor = Executors.newFixedThreadPool(numberOfTimes)
    val task = new Runnable {
      override def run(): Unit = {
        await(ws.url("/route").get())
      }
    }
    for (i <- 1 to numberOfTimes) executor.submit(task)

    executor.awaitTermination(1, TimeUnit.SECONDS)

    route.timeCalled shouldEqual numberOfTimes

    ws.close()
  }

}
