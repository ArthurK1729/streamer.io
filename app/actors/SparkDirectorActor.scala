package actors

import javax.inject.Inject

import actors.SparkActor.LaunchSparkJob
import akka.actor.{Actor, ActorRef}
import play.api.libs.concurrent.InjectedActorSupport
import play.api.{Configuration, Logger}

import scala.collection.mutable

object SparkDirectorActor {
  case class ScheduleSparkJob(jobId: String, mlAlgorithm: String, kafkaSourceTopic: String, kafkaDestinationTopic: String)

  trait Factory {
    def apply(config: String): Actor
  }
}

class SparkDirectorActor @Inject()(configuration: Configuration, ingestionActorFactory: SparkActor.Factory)
  extends Actor with InjectedActorSupport {

  val ingestionPrefix = configuration.get[String]("spark.prefix")

  import SparkDirectorActor._

  val sparkActors: mutable.Map[String, (ActorRef, Process)] = mutable.Map()

  def receive = {
    case ScheduleSparkJob(jobId, mlAlgorithm, kafkaSourceTopic, kafkaDestinationTopic) =>
      Logger.info("Spark director has received message: " + ScheduleSparkJob.toString)
      val sparkActor: ActorRef = injectedChild(ingestionActorFactory("key"),
        configuration.get[String]("spark.prefix") + jobId)

      sparkActor ! LaunchSparkJob
  }

}
