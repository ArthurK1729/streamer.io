package actors

import java.util.UUID
import javax.inject._

import actors.IngestionDirectorActor.{ScheduleIngestionJob, StopIngestionJobs}
import actors.SparkDirectorActor.{ScheduleSparkJob, StopSparkJob}
import akka.actor._
import akka.util.Timeout
import model.JobInfo
import play.api.{Configuration, Logger}
import play.api.libs.concurrent.InjectedActorSupport

import scala.concurrent.duration._

//import play.api.Configuration

object DirectorActor {
  case object RequestSparkJob
  case object RequestIngestionJob
  case class RequestStopIngestionJob(jobId: String)
  case class CreateNewJob(jobInfo: JobInfo)
  case class StopJobCompletely(jobId: String)
}

class DirectorActor @Inject()(@Named("ingestionDirector") ingestionDirector: ActorRef,
                              @Named("sparkDirector") sparkDirector: ActorRef,
                              configuration: Configuration) extends Actor with InjectedActorSupport {
  import DirectorActor._
  implicit val timeout = Timeout(20 seconds)

  // Generate a single ID here to be able to correlate ingestion actors and the respective Spark jobs
  // Receive a single CreateJob request from the UI and delegate tasks to Ingestion/Spark directors
  def receive = {
    case msg @ RequestSparkJob =>
      Logger.info("Director received message: " + msg.toString)
      sparkDirector ! ScheduleSparkJob

    case msg @ RequestIngestionJob =>
      Logger.info("Director received message: " + msg.toString)
      Logger.info("Director is forwarding RequestIngestionJob to " + ingestionDirector.path.name)

      ingestionDirector.forward(ScheduleIngestionJob)

    case msg @ RequestStopIngestionJob(jobId) =>
      Logger.info("Director received message: " + msg.toString)
      ingestionDirector ! StopIngestionJobs(jobId)

    case msg @ CreateNewJob(jobInfo) =>
      Logger.info("Director received message: " + msg.toString)
      val jobUUID = UUID.randomUUID().toString

      //TODO: use kafka prefixes
      val kafkaSourceTopic = "test"
      val kafkaDestinationTopic = "testResult"

      Logger.info("Director generated id for new job: " + jobUUID)

      jobInfo.sources.foreach { source =>
        ingestionDirector ! ScheduleIngestionJob(jobUUID, source, kafkaSourceTopic)
      }

      sparkDirector ! ScheduleSparkJob(jobUUID, jobInfo.mlAlgorithm, kafkaDestinationTopic, kafkaSourceTopic)

      sender() ! jobUUID

    case msg @ StopJobCompletely(jobId) =>
      Logger.info("Director received message: " + msg.toString)
      ingestionDirector ! StopIngestionJobs(jobId)
      sparkDirector ! StopSparkJob(jobId)
  }
}