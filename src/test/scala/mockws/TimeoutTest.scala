package mockws

import java.util.concurrent.TimeoutException

import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.time.{Milliseconds, Span}
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}
import play.api.mvc.Result

import scala.concurrent.Promise
import scala.concurrent.duration._

class TimeoutTest extends FunSuite with Matchers with MockWSHelpers with BeforeAndAfterAll {

  /**
   * Given a route that hangs forever, a request timeout of 1 ms should fail the future within 500 ms.
   */
  test("request timeout should fail calls that don't complete") {
    implicit val patienceConfig = PatienceConfig(timeout = Span(500, Milliseconds))

    val ws = MockWS {
      case (_, "/hang/forever") => Action.async(Promise[Result]().future)
    }

    val futureResponse = ws.url("/hang/forever").withRequestTimeout(1.millis).get()

    whenReady(futureResponse.failed)(_ shouldBe a[TimeoutException])

    ws.close()
  }

  override def afterAll(): Unit = {
    shutdownHelpers()
  }

}
