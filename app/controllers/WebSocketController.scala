package controllers

import java.util.Properties
import javax.inject._

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import play.api.libs.json._
import play.api.libs.ws._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


@Singleton
class WebSocketController @Inject() (cc: ControllerComponents, ws: WSClient) extends AbstractController(cc) {
  def getStream = Action.async { implicit request =>
    val URL = """https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=MSFT&interval=1min&apikey=VVF9X8MSOTTULA37"""

    val wsRequest: WSRequest = ws.url(URL)
      .addHttpHeaders("Accept" -> "application/json")
      .withRequestTimeout(10000.millis)

    val futureResponse: Future[JsValue] = wsRequest.get().map { response =>
      //(response.json \ "Meta Data" \ "1. Information").as[String]
      response.json
    }

    val kafkaProps = new Properties()
    kafkaProps.put("bootstrap.servers", "localhost:9092")
    kafkaProps.put("client.id", "ScalaProducerExample")
    kafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    kafkaProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](kafkaProps)

    val apiOutput = Future[Result] {
        val dataForKafka = new ProducerRecord[String, String]("test", "localhost", Await.result(futureResponse, 20 seconds).toString)
        producer.send(dataForKafka)
        producer.close()
        Ok("Output sent to Kafka")
    }

    apiOutput
  }
}
