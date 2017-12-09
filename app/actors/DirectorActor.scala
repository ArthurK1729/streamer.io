package actors

import javax.inject._

import actors.IngestionDirectorActor.{ScheduleIngestionJob, StopIngestionJob}
import actors.SparkDirectorActor.ScheduleSparkJob
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import model.JobInfo
import play.api.libs.concurrent.InjectedActorSupport
import play.api.Logger

import scala.concurrent.Await
import scala.concurrent.duration._

//import play.api.Configuration

object DirectorActor {
  case object RequestSparkJob
  case object RequestIngestionJob
  case class RequestStopIngestionJob(jobId: String)
  case class CreateNewJob(jobInfo: JobInfo)
}

class DirectorActor @Inject()(@Named("ingestionDirector") ingestionDirector: ActorRef,
                              @Named("sparkDirector") sparkDirector: ActorRef) extends Actor with InjectedActorSupport {
  import DirectorActor._
  implicit val timeout = Timeout(20 seconds)

  // Generate a single ID here to be able to correlate ingestion actors and the respective Spark jobs
  // Receive a single CreateJob request from the UI and delegate tasks to Ingestion/Spark directors
  def receive = {
    case RequestSparkJob =>
      Logger.info("Director received message: " + RequestSparkJob.toString)
      sparkDirector ! ScheduleSparkJob

    case RequestIngestionJob =>
      Logger.info("Director received message: " + RequestIngestionJob.toString)
      Logger.info("Director is forwarding RequestIngestionJob to " + ingestionDirector.path.name)

      //val jobId = Await.result(ingestionDirector ? ScheduleIngestionJob, 20 seconds).asInstanceOf[String]


      ingestionDirector.forward(ScheduleIngestionJob)

//      Logger.info("Job id of new job: " + jobId)
//      sender ! jobId

    case RequestStopIngestionJob(jobId) =>
      Logger.info("Director received message: " + RequestStopIngestionJob.toString)
      ingestionDirector ! StopIngestionJob(jobId)
  }
}