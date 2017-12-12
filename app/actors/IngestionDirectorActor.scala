package actors

import java.util.Properties
import javax.inject._
import java.util.UUID

import actors.IngestionActor.DoRestRequest
import akka.actor._
import model.SourceInfo
import org.apache.kafka.clients.producer.KafkaProducer
import play.api.{Configuration, Logger}
import play.api.libs.concurrent.InjectedActorSupport

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable

//import play.api.Configuration

object IngestionDirectorActor {
  case class ScheduleIngestionJob(jobId: String, sourceInfo: SourceInfo, kafkaIngestionTopic: String)
  case class StopIngestionJob(jobId: String)

  trait Factory {
    def apply(config: String): Actor
  }
}

class IngestionDirectorActor @Inject()(configuration: Configuration, ingestionActorFactory: IngestionActor.Factory)
  extends Actor with InjectedActorSupport {
  import IngestionDirectorActor._

  val ingestionActors: mutable.Map[String, mutable.Map[String, (ActorRef, Cancellable)]] = mutable.Map()
  val ingestionPrefix = configuration.get[String]("ingestion.prefix")

  def receive = {
    case ScheduleIngestionJob(jobId, sourceInfo, kafkaIngestionTopic) =>
      //TODO: Check if already in the map?
      Logger.info("Ingestion director received message: " + ScheduleIngestionJob.toString)

      val ingestionActorJobId = ingestionPrefix + jobId + "-" + sourceInfo.sourceName

      val kafkaProps = new Properties()
      kafkaProps.put("bootstrap.servers", "localhost:9092")
      kafkaProps.put("client.id", ingestionActorJobId)
      kafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
      kafkaProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

      val producer = new KafkaProducer[String, String](kafkaProps)

      Logger.info("Kafka producer set with options: " + kafkaProps.toString)

      val ingestionActor: ActorRef = injectedChild(ingestionActorFactory(producer,
        sourceInfo.sourceURL,
        kafkaIngestionTopic
      ), ingestionActorJobId)

      val pollingHandle =
        context.system.scheduler.schedule(
          Duration.Zero,
          sourceInfo.pollingFrequencySeconds.toInt seconds,
          ingestionActor,
          DoRestRequest)

      //TODO: Synchronise this with a map of jobs on the UI
      if (ingestionActors isDefinedAt jobId) {
        ingestionActors(jobId) += (ingestionActorJobId -> (ingestionActor, pollingHandle))
      } else {
        ingestionActors.put(jobId, mutable.Map())
        ingestionActors(jobId) += (ingestionActorJobId -> (ingestionActor, pollingHandle))
      }

    case StopIngestionJob(jobId) =>
      Logger.info("Ingestion director received message: " + StopIngestionJob.toString)
      ingestionActors(jobId).values.foreach { ingestionActor =>
        ingestionActor._2.cancel()
      }
  }


}
