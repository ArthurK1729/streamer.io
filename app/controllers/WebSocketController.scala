package controllers

import javax.inject._

import scala.concurrent.Future
import scala.concurrent.duration._
import play.api.mvc._
import play.api.libs.ws._
import play.api.libs.json._
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.http.HttpEntity
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.util.ByteString
import play.api.libs.json.{JsError, JsPath, Json, Reads}
import play.api.libs.functional.syntax._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

@Singleton
class WebSocketController @Inject() (cc: ControllerComponents, ws: WSClient) extends AbstractController(cc) {
  def getStream = Action.async { implicit request =>
    val URL = """https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=MSFT&interval=1min&apikey=VVF9X8MSOTTULA37"""

    val wsRequest: WSRequest = ws.url(URL)
      .addHttpHeaders("Accept" -> "application/json")
      .withRequestTimeout(10000.millis)

    val futureResponse: Future[Result] = wsRequest.get().map { response =>
      Ok((response.json \ "Meta Data" \ "1. Information").as[String])
    }

    futureResponse
  }
}
