package mockws

import akka.actor
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.mvc.{DefaultActionBuilder, PlayBodyParsers}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration.Duration

object Helpers {
  private val ActorSystem: ActorSystem = actor.ActorSystem("unit-testing")
  implicit val Materializer: ActorMaterializer = ActorMaterializer()(ActorSystem)
  val BodyParser: PlayBodyParsers = PlayBodyParsers()
  val Action: DefaultActionBuilder = DefaultActionBuilder(BodyParser.anyContent)

  sys addShutdownHook {
    Materializer.shutdown()
    Await.result(ActorSystem.terminate(), Duration.Inf)
  }
}
