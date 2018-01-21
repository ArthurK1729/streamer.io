package actors

import java.io.File
import javax.inject.Inject

import actors.SparkActor.LaunchSparkJob
import akka.actor.{Actor, ActorRef}
import org.apache.spark.launcher.SparkLauncher
import play.api.libs.concurrent.InjectedActorSupport
import play.api.{Configuration, Logger}

import scala.collection.mutable

object SparkDirectorActor {
  case class ScheduleSparkJob(jobId: String,
                              mlAlgorithm: String,
                              kafkaSourceTopic: String,
                              kafkaDestinationTopic: String)
  case class StopSparkJob(jobId: String)
  case class RegisterNewSparkActor(sparkHandle: Process, jobId: String)

  trait Factory {
    def apply(config: String): Actor
  }
}

class SparkDirectorActor @Inject()(configuration: Configuration, ingestionActorFactory: SparkActor.Factory)
  extends Actor with InjectedActorSupport {

  val ingestionPrefix = configuration.get[String]("spark.prefix")

  import SparkDirectorActor._

  //TODO: Replace everything with synchronized maps to protect from race conds?
  val sparkActors: mutable.Map[String, (ActorRef, Process)] = mutable.Map()

  def receive = {
    case msg @ ScheduleSparkJob(jobId, mlAlgorithm, kafkaSourceTopic, kafkaDestinationTopic) =>
      Logger.info("Spark director has received message: " + msg.toString)



      val sparkActor: ActorRef = injectedChild(ingestionActorFactory(),
        configuration.get[String]("spark.prefix") + jobId)



      sparkActor ! LaunchSparkJob

    case msg @ RegisterNewSparkActor(sparkHandle: Process, jobId: String) =>
      Logger.info("Spark director has received message: " + msg.toString)
      sparkActors += (jobId -> (sender(), sparkHandle))

    case msg @ StopSparkJob(jobId) =>
      Logger.info("Spark director has received message: " + msg.toString)
      Logger.info("Destroying Spark job with id: " + sparkActors(jobId)._1.path.name)
      sparkActors(jobId)._2.destroy()
  }

}
