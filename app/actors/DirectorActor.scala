package actors

import javax.inject._

import actors.IngestionDirectorActor.{ScheduleIngestionJob, StopIngestionJob}
import actors.SparkDirectorActor.ScheduleSparkJob
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import play.api.libs.concurrent.InjectedActorSupport
import scala.concurrent.duration._

//import play.api.Configuration

object DirectorActor {
  case object RequestSparkJob
  case object RequestIngestionJob
  case class RequestStopIngestionJob(jobId: String)
}

class DirectorActor @Inject()(@Named("ingestionDirector") ingestionDirector: ActorRef,
                              @Named("sparkDirector") sparkDirector: ActorRef) extends Actor with InjectedActorSupport {
  import DirectorActor._
  implicit val timeout = Timeout(20 seconds)

  def receive = {
    case RequestSparkJob =>
      sparkDirector ! ScheduleSparkJob

    case RequestIngestionJob =>
      ingestionDirector ? ScheduleIngestionJob

    case RequestStopIngestionJob(jobId) =>
      ingestionDirector ! StopIngestionJob(jobId)
  }
}