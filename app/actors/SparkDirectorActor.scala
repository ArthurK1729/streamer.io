package actors

import javax.inject.Inject

import actors.SparkActor.LaunchSparkJob
import akka.actor.{Actor, ActorRef}
import play.api.libs.concurrent.InjectedActorSupport
import play.api.Logger

import scala.collection.mutable

object SparkDirectorActor {
  case object ScheduleSparkJob

  trait Factory {
    def apply(config: String): Actor
  }
}

class SparkDirectorActor @Inject()(ingestionActorFactory: SparkActor.Factory)
  extends Actor with InjectedActorSupport {

  import SparkDirectorActor._

  val sparkActors: mutable.Map[String, (ActorRef, Process)] = mutable.Map()

  def receive = {
    case ScheduleSparkJob =>
      Logger.info("Spark director has received message: " + ScheduleSparkJob.toString)
      val sparkActor: ActorRef = injectedChild(ingestionActorFactory("key"), "spark-actor-1")

      sparkActor ! LaunchSparkJob
  }

}