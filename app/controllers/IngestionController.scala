package controllers

import javax.inject._

import actors.DirectorActor.{RequestIngestionJob, RequestStopIngestionJob}
import akka.actor.ActorRef
import akka.util.Timeout
import play.api.libs.ws._
import play.api.mvc._
import akka.pattern.ask

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}


@Singleton
class IngestionController @Inject()(@Named("director") director: ActorRef,
                                    cc: ControllerComponents, ws: WSClient)
                                   (implicit ec: ExecutionContext) extends AbstractController(cc) {

  implicit val timeout = Timeout(20 seconds)

  def getStream = Action.async { implicit request =>
    val jobId: String = Await.result(director ? RequestIngestionJob, 20 seconds).asInstanceOf[String]

    Future.successful(Ok("New job id is " + jobId))
  }

  def stopStream = Action.async { implicit request =>
    director ! RequestStopIngestionJob

    Future.successful(Ok("Job stopped"))
  }
}
