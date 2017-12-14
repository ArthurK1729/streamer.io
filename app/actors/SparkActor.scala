package actors

import java.io.File

import akka.actor._
import javax.inject._

import actors.SparkDirectorActor.RegisterNewSparkActor
import com.google.inject.assistedinject.Assisted
import org.apache.spark.launcher.SparkLauncher
import play.api.Logger

object SparkActor {
  case object LaunchSparkJob

  // This trait is autoimplemented by guice
  trait Factory {
    def apply(): Actor
  }
}

class SparkActor @Inject()() extends Actor {
  import SparkActor._

  def receive = {
    case msg @ LaunchSparkJob =>
      Logger.info(self.path.name + " has received message: " + msg.toString)
      // Replace most of below with config calls
      val sparkHandle = new SparkLauncher()
        .setAppResource("/home/osboxes/IdeaProjects/streamer.io/SparkProject/target/scala-2.11/sparkproj_2.11-1.jar")
        .setMainClass("main.SparkLauncher")
        .setMaster("local")
        .setSparkHome("/usr/local/spark")
        .setConf(SparkLauncher.DRIVER_MEMORY, "2g")
        .addSparkArg("--packages", "org.apache.spark:spark-streaming-kafka-0-10_2.11:2.2.0")
        .redirectError(new File("/home/osboxes/utility_scripts/spark.log"))
        .launch


      sender() ! RegisterNewSparkActor(sparkHandle, self.path.name.stripPrefix("spark-actor-"))

      Logger.info(self.path.name + " has successfully launched a Spark job")
      sparkHandle.waitFor()
  }

  override def postStop(): Unit = {
    Logger.info("Destroying Spark actor for " + self.path.name)
    // Figure out a way to self destruct
   // process.destroy
  }
}