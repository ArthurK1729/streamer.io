package actors

import akka.actor._
import javax.inject._

import actors.SparkActor.LaunchSparkJob
import actors.WebSocketActor.LaunchWebSocketJob
import play.api.libs.concurrent.InjectedActorSupport

//import play.api.Configuration

object DirectorActor {
  case object InitialiseSparkJob
  case object InitialiseWebSocketJob
}

class DirectorActor @Inject()(sparkActorFactory: SparkActor.Factory,
                              websocketActorFactory: WebSocketActor.Factory) extends Actor with InjectedActorSupport {
  import DirectorActor._

  def receive = {
    case InitialiseSparkJob =>
      val sparkActor: ActorRef = injectedChild(sparkActorFactory("key"), "spark-actor-1")
      sparkActor ! LaunchSparkJob

    case InitialiseWebSocketJob =>
      val websocketActor: ActorRef = injectedChild(websocketActorFactory("key"), "websocket-actor-1")
      websocketActor ! LaunchWebSocketJob
  }
}