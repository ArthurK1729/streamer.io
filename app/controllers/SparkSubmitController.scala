package controllers

import javax.inject._

import actors.DirectorActor.RequestSparkJob
import akka.actor.ActorRef
import play.api.mvc._
import play.api.Logger

import scala.concurrent.ExecutionContext

@Singleton
class SparkSubmitController @Inject() (@Named("director") director: ActorRef,
                                       cc: ControllerComponents)
                                      (implicit ec: ExecutionContext) extends AbstractController(cc) {
  def launchSpark() = Action {
    Logger.info("SparkSubmitController.launchSpark endpoint has been hit.")

    director ! RequestSparkJob
    Ok("Request for Spark job sent")
  }
}
