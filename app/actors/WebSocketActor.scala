package actors

import java.util.Properties
import javax.inject._

import akka.actor._
import com.google.inject.assistedinject.Assisted
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import play.api.libs.json.JsValue
import play.api.libs.ws.{WSClient, WSRequest}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

object WebSocketActor {
  case object LaunchWebSocketJob

  // This trait is autoimplemented by guice
  trait Factory {
    def apply(config: String): Actor
  }
}

class WebSocketActor @Inject() (ws: WSClient,
                                @Assisted config: String)
                               (implicit ec: ExecutionContext) extends Actor {
  import WebSocketActor._

  def receive = {
    case LaunchWebSocketJob =>
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

      val dataForKafka = new ProducerRecord[String, String]("test", "localhost", Await.result(futureResponse, 20 seconds).toString)
      producer.send(dataForKafka)
      producer.close()
  }
}