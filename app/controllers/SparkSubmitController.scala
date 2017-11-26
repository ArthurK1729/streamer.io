package controllers

import java.util.Properties
import javax.inject._

import org.apache.commons.io.IOUtils
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.spark.launcher.SparkLauncher
import play.api.libs.json._
import play.api.libs.ws._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import java.io.File

@Singleton
class SparkSubmitController @Inject() (cc: ControllerComponents) extends AbstractController(cc) {
  def launchSpark() = Action {
    // This returns a Spark Handle which can alter be added to a job manager (Process atm)
    val spark = new SparkLauncher()
      .setAppResource("/home/osboxes/IdeaProjects/streamer.io/SparkProject/target/scala-2.11/sparkproj_2.11-1.jar")
      .setMainClass("main.SparkLauncher")
      .setMaster("local")
      .setSparkHome("/usr/local/spark")
      .setConf(SparkLauncher.DRIVER_MEMORY, "2g")
      .addSparkArg("--packages", "org.apache.spark:spark-streaming-kafka-0-10_2.11:2.2.0")
      .redirectError(new File("/home/osboxes/utility_scripts/spark.log"))
      .launch



    //IOUtils.copy(spark.getErrorStream, )
    // This obviously needs to be in an actor and async
    spark.waitFor()
    Ok("...")
  }
}
