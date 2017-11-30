package actors

import javax.inject._

import akka.actor._
import com.google.inject.assistedinject.Assisted
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import play.api.libs.json.JsValue
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.Logger

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

object IngestionActor {
  case object DoRestRequest

  // This trait is autoimplemented by guice
  trait Factory {
    def apply(producer: KafkaProducer[String, String]): Actor
  }
}

class IngestionActor @Inject()(ws: WSClient,
                               @Assisted producer: KafkaProducer[String, String])
                              (implicit ec: ExecutionContext) extends Actor {
  import IngestionActor._

  def receive = {
    case DoRestRequest =>
      Logger.info(self.path.name + " has received message: " + DoRestRequest.toString)
      val URL = """https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=MSFT&interval=1min&apikey=VVF9X8MSOTTULA37"""

      val wsRequest: WSRequest = ws.url(URL)
        .addHttpHeaders("Accept" -> "application/json")
        .withRequestTimeout(20000 millis)

      val futureResponse: Future[JsValue] = wsRequest.get().map { response =>
        //(response.json \ "Meta Data" \ "1. Information").as[String]
        response.json
      }

      val dataForKafka = new ProducerRecord[String, String]("test", "localhost", Await.result(futureResponse, 20 seconds).toString)
      Logger.debug("Data to store in Kafka: " + dataForKafka.toString)

      producer.send(dataForKafka)
  }

  override def postStop(): Unit = {
    Logger.info("Closing the producer for " + self.path.name)
    producer.close()
  }
}