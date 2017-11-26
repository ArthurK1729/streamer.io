package actors

import java.io.File

import akka.actor._
import javax.inject._

import com.google.inject.assistedinject.Assisted
import org.apache.spark.launcher.SparkLauncher

object SparkActor {
  case object LaunchSparkJob

  // This trait is autoimplemented by guice
  trait Factory {
    def apply(config: String): Actor
  }
}

class SparkActor @Inject() (@Assisted config: String) extends Actor {
  import SparkActor._

  def receive = {
    case LaunchSparkJob =>
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
      // This obviously needs to be in an actor and async
      spark.waitFor()
  }
}