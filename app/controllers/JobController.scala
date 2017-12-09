package controllers

import javax.inject._

import actors.DirectorActor.{RequestIngestionJob, RequestStopIngestionJob}
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import play.api.Logger
import play.api.libs.json.{Reads, _}
import play.api.libs.ws._
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

case class StopJobRequest(jobId: String)

@Singleton
class JobController @Inject()(@Named("director") director: ActorRef,
                              cc: ControllerComponents, ws: WSClient)
                             (implicit ec: ExecutionContext) extends AbstractController(cc) {

  implicit val timeout = Timeout(20 seconds)

  def getStream = Action.async { implicit request =>
    Logger.info("IngestionController.getStream endpoint hit with request: " + request.toString)

    val jobId: String = Await.result(director ? RequestIngestionJob, 20 seconds).asInstanceOf[String]

    Future.successful(Ok("New job id is " + jobId))
  }

  def stopStream = Action.async(parse.json) { implicit request =>
    implicit val stopJobRequestReads: Reads[StopJobRequest] = (__ \ "jobId").read[String].map { jobId => StopJobRequest(jobId) }

    val stopJobRequest = request.body.as[StopJobRequest]

    Logger.info("IngestionController.stopStream endpoint hit with request: " + request.toString)
    director ! RequestStopIngestionJob(stopJobRequest.jobId)

    Future.successful(Ok("Job stopped"))
  }

  def createNewJob = Action.async(parse.json) { implicit request =>


    Future.successful(Ok("temp"))
  }
}
