package controllers

import javax.inject._

import actors.DirectorActor.{CreateNewJob, RequestIngestionJob, RequestStopIngestionJob}
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import model.{JobInfo, SourceInfo}
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, _}
import play.api.libs.ws._
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

case class StopJobRequest(jobId: String)

@Singleton
class JobController @Inject()(@Named("director") director: ActorRef,
                              cc: ControllerComponents)
                             (implicit ec: ExecutionContext) extends AbstractController(cc) {

  implicit val timeout = Timeout(20 seconds)

  def getStream = Action.async { implicit request =>
    Logger.info("JobController.getStream endpoint hit with request: " + request.toString)

    val jobId: Result = Await.result(director ? RequestIngestionJob, 20 seconds).asInstanceOf[Result]

    // Try this later
    //(director ? RequestIngestionJob).asInstanceOf[Future[Result]]
    Future.successful(Ok("New job id is " + jobId))
  }

  def stopStream = Action.async(parse.json) { implicit request =>
    implicit val stopJobRequestReads: Reads[StopJobRequest] = (__ \ "jobId").read[String].map { jobId => StopJobRequest(jobId) }

    val stopJobRequest = request.body.as[StopJobRequest]

    Logger.info("JobController.stopStream endpoint hit with request: " + request.toString)
    director ! RequestStopIngestionJob(stopJobRequest.jobId)

    Future.successful(Ok("Job stopped"))
  }

  def createNewJob = Action.async(parse.json) { implicit request =>
    implicit val sourceInfoReads: Reads[SourceInfo] = (
      (JsPath \ "sourceName").read[String]
        and (JsPath \ "sourceURL").read[String]
        and (JsPath \ "webSocket").read[Boolean]
        and (JsPath \ "jsonSelectionThing").read[String]
        and (JsPath \ "pollingFrequencySeconds").read[String]
      )(SourceInfo.apply _)

    implicit val jobInfoReads: Reads[JobInfo] = (
      (JsPath \ "sources").read[Seq[SourceInfo]]
        and (JsPath \ "mlAlgorithm").read[String]
      )(JobInfo.apply _)


    Logger.info("JobController.createNewJob endpoint hit with request: " + request.toString)

    val jobJsonBody = request.body.validate[JobInfo]

    jobJsonBody.fold(
      errors => {
        BadRequest(Json.obj("message" -> JsError.toJson(errors)))
      },
      componentDetails => {
        //TODO: Needs to give you back the ID of the job
        director ! CreateNewJob(request.body.as[JobInfo])
      }
    )

    Future.successful(Ok("temp"))
  }
}
