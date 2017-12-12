package actors

import javax.inject._

import akka.actor._
import com.google.inject.assistedinject.Assisted
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import play.api.Logger
import play.api.libs.ws.{WSClient, WSRequest}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object IngestionActor {
  case object DoRestRequest
  case class StoreInKafka(data: String)

  // This trait is autoimplemented by guice
  trait Factory {
    def apply(@Assisted("producer") producer: KafkaProducer[String, String], @Assisted("sourceURL") sourceURL: String, @Assisted("kafkaTopic") kafkaTopic: String): Actor
  }
}




class IngestionActor @Inject()(ws: WSClient,
                               @Assisted("producer") producer: KafkaProducer[String, String],
                               @Assisted("sourceURL") sourceURL: String,
                               @Assisted("kafkaTopic") kafkaTopic: String)
                              (implicit ec: ExecutionContext) extends Actor {
  import IngestionActor._

  def receive = {
    case DoRestRequest =>
      Logger.info(self.path.name + " has received message: " + DoRestRequest.toString)

      val wsRequest: WSRequest = ws.url(sourceURL)
        .addHttpHeaders("Accept" -> "application/json")
        .withRequestTimeout(20000 millis)

      wsRequest.get().map { response =>
        //(response.json \ "Meta Data" \ "1. Information").as[String]
        response.json
      }.onComplete {
        case Success(data) =>
          val dataForKafka = new ProducerRecord[String, String](kafkaTopic, "localhost", data.toString)
          Logger.debug("Data to store in Kafka: " + dataForKafka.toString)
          producer.send(dataForKafka)
        case Failure(error) =>
          // TODO: Error handling
          Logger.error(error.toString)
      }
  }

  override def postStop(): Unit = {
    Logger.info("Closing the producer for " + self.path.name)
    producer.close()
  }
}