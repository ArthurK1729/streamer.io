import scala.sys.process._

name := """Streamer.io"""

version := "1.0-SNAPSHOT"
// Might need to downgrade Kafka below to 0.10 to match spark-streaming-kafka-0-10
lazy val depForPlay = Seq(
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
  "com.h2database" % "h2" % "1.4.196",
  ws,
  "org.apache.kafka" % "kafka_2.11" % "1.0.0"
)

lazy val root = (project in file("."))
  .settings(
    scalaVersion := "2.11.8",
    libraryDependencies ++= depForPlay
  ).enablePlugins(PlayScala)

resolvers += Resolver.sonatypeRepo("snapshots")

resolvers ++= Seq(
  "JBoss Repository" at "http://repository.jboss.org/nexus/content/repositories/releases/",
  "Spray Repository" at "http://repo.spray.cc/",
  "Cloudera Repository" at "https://repository.cloudera.com/artifactory/cloudera-repos/",
  "Akka Repository" at "http://repo.akka.io/releases/",
  "Twitter4J Repository" at "http://twitter4j.org/maven2/",
  "Apache HBase" at "https://repository.apache.org/content/repositories/releases",
  "Twitter Maven Repo" at "http://maven.twttr.com/",
  "scala-tools" at "https://oss.sonatype.org/content/groups/scala-tools",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Second Typesafe repo" at "http://repo.typesafe.com/typesafe/maven-releases/",
  "Mesosphere Public Repository" at "http://downloads.mesosphere.io/maven",
  Resolver.sonatypeRepo("public")
)





lazy val sparkSubmit = taskKey[Unit]("Execute spark-submit")

lazy val depForSpark = Seq(
  "org.apache.spark" % "spark-core_2.11" % "2.2.0" % "provided",
  "org.apache.spark" % "spark-sql_2.11" % "2.2.0" % "provided",
  "org.apache.spark" % "spark-streaming_2.11" % "2.2.0",
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.2.0"
)
// Use assembly plugin to package everything?
lazy val sparkProj = (project in file("./SparkProject")).settings(
  organization := "test",
  version := "1",
  libraryDependencies ++= depForSpark,
  scalaVersion := "2.11.8",
  sparkSubmit := {
    "/home/osboxes/IdeaProjects/streamer.io/SparkProject/scripts/sparkSubmit.sh" !
  }
)


