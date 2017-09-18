package mockws

import akka.actor
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.mvc.{DefaultActionBuilder, PlayBodyParsers}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration.Duration

/**
  * The trait provides a materializer you need in order to
  * use MockWS and its Action { ... } definitions inside your testclasses.
  *
  * Mix this trait into the tests where you use MockWS.
  *
  * You can also use the object if you dislike traits and like to instead import the functionality.
  *
  * Example:
  *
  * {{{
  * class MySpec extends FreeSpec with Matchers with MockWSHelpers {
  *    ...
  * }
  * }}}
  *
  */
trait MockWSHelpers {
  private val ActorSystem: ActorSystem = actor.ActorSystem("unit-testing")
  implicit val Materializer: ActorMaterializer = ActorMaterializer()(ActorSystem)
  val BodyParser: PlayBodyParsers = PlayBodyParsers()
  val Action: DefaultActionBuilder = DefaultActionBuilder(BodyParser.anyContent)

  sys addShutdownHook {
    Materializer.shutdown()
    Await.result(ActorSystem.terminate(), Duration.Inf)
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
object MockWSHelpers extends MockWSHelpers
