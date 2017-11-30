package actors

import java.util.Properties
import javax.inject._
import java.util.UUID

import actors.IngestionActor.DoRestRequest
import akka.actor._
import org.apache.kafka.clients.producer.KafkaProducer
import play.api.Logger
import play.api.libs.concurrent.InjectedActorSupport

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable

//import play.api.Configuration

object IngestionDirectorActor {
  case object ScheduleIngestionJob
  case class StopIngestionJob(jobId: String)

  trait Factory {
    def apply(config: String): Actor
  }
}

class IngestionDirectorActor @Inject()(ingestionActorFactory: IngestionActor.Factory)
  extends Actor with InjectedActorSupport {

  import IngestionDirectorActor._


  val ingestionActors: mutable.Map[String, (ActorRef, Cancellable)] = mutable.Map()

  def receive = {
    case ScheduleIngestionJob =>
      Logger.info("Ingestion director received message: " + ScheduleIngestionJob.toString)

      val kafkaProps = new Properties()
      kafkaProps.put("bootstrap.servers", "localhost:9092")
      kafkaProps.put("client.id", "ScalaProducerExample")
      kafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
      kafkaProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

      val producer = new KafkaProducer[String, String](kafkaProps)

      Logger.info("Kafka producer set with options: " + kafkaProps.toString)

      val jobUUID = UUID.randomUUID().toString
      val jobId = "ingestion-actor-" + jobUUID

      val ingestionActor: ActorRef = injectedChild(ingestionActorFactory(producer), jobId)
      val cancellable =
        context.system.scheduler.schedule(
          Duration.Zero,
          5 seconds,
          ingestionActor,
          DoRestRequest)

      // Synchronise this with a map of jobs on the UI
      ingestionActors += (jobId -> (ingestionActor, cancellable))

      sender() ! jobId

    case StopIngestionJob(jobId) =>
      Logger.info("Ingestion director received message: " + StopIngestionJob.toString)
      ingestionActors(jobId)._2.cancel()
  }


}
