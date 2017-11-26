package controllers

import javax.inject._

import actors.DirectorActor.InitialiseWebSocketJob
import akka.actor.ActorRef
import akka.util.Timeout
import play.api.libs.ws._
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class WebSocketController @Inject() (@Named("director") director: ActorRef,
                                     cc: ControllerComponents, ws: WSClient)
                                    (implicit ec: ExecutionContext) extends AbstractController(cc) {

  implicit val timeout: Timeout = 5 seconds

  def getStream = Action.async { implicit request =>
    director ! InitialiseWebSocketJob

    Future.successful(Ok("Output sent to Kafka"))
  }
}
