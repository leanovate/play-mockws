package mockws

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.mvc.{DefaultActionBuilder, PlayBodyParsers}
import scala.concurrent.ExecutionContext.Implicits._

object Helpers {
  implicit val as = ActorSystem()
  implicit val materializer = ActorMaterializer()
  val bodyParser = PlayBodyParsers()
  val action = DefaultActionBuilder(bodyParser.anyContent)
}
