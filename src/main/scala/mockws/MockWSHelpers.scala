package mockws

import akka.actor
import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.mvc.DefaultActionBuilder
import play.api.mvc.PlayBodyParsers

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._

/**
 * The trait provides a materializer you need in order to
 * use MockWS and its Action { ... } definitions inside your testclasses.
 *
 * Mix this trait into the tests where you use MockWS.
 * WARNING: you have to call `shutdownHelpers()` after usage to avoid memory leaks.
 *
 * You can also use the object if you dislike traits and like to instead import the functionality.
 *
 * Example:
 *
 * {{{
 * class MySpec extends FreeSpec with Matchers with MockWSHelpers with BeforeAndAfterAll {
 *    ...
 *
 *   override def afterAll(): Unit = {
 *     shutdownHelpers()
 *   }
 * }
 * }}}
 */
trait MockWSHelpers {
  private val actorSystem: ActorSystem    = actor.ActorSystem("unit-testing")
  implicit val materializer: Materializer = Materializer.matFromSystem(actorSystem)
  val BodyParser: PlayBodyParsers         = PlayBodyParsers()
  val Action: DefaultActionBuilder        = DefaultActionBuilder(BodyParser.anyContent)

  def shutdownHelpers(): Unit = {
    materializer.shutdown()
    Await.result(actorSystem.terminate(), 3.minutes)
  }

}

/**
 * Tiny helper so that you can import the functionality of MockWSHelper instead of extending your test class with
 * a trait.
 *
 * {{{
 * import mockws.MockWSHelpers._
 * }}}
 */
object MockWSHelpers extends MockWSHelpers {
  sys.addShutdownHook {
    shutdownHelpers()
  }
}
