package main

import java.util.{Properties, UUID}

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.spark.streaming.{Seconds, StreamingContext}
//import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.{LabeledPoint, StreamingLinearRegressionWithSGD}
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010._
import org.json4s._
import org.json4s.jackson.JsonMethods._

object SparkLauncher {
  def main(args: Array[String]): Unit = {
//    println("STARTING COMPUTATION")
//    val logFile = "/usr/local/spark/README.md"
//    val spark = SparkSession.builder.appName("Simple Application").getOrCreate()
//    val logData = spark.read.textFile(logFile).cache()
//    val numAs = logData.filter(line => line.contains("a")).count()
//    val numBs = logData.filter(line => line.contains("b")).count()
//    println(s"Lines with a: $numAs, Lines with b: $numBs")
//    spark.stop()




//    val conf = new SparkConf().setMaster("local[*]").setAppName("KafkaReceiver")
//    val ssc  = new StreamingContext(conf, Seconds(10))
//    val kafkaStream = KafkaUtils.createStream(ssc, "localhost:2181", "spark-streaming-consumer-group", Map("test" -> 5))
//    kafkaStream.print
//    ssc.start

    val conf = new SparkConf().setMaster("local[2]").setAppName("SparkStreaming")

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "spark-playground-group",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )
    val ssc = new StreamingContext(conf, Seconds(1))
    val sc = ssc.sparkContext
    //sc.setLogLevel("ERROR")

    val kafkaProps = new Properties()
    kafkaProps.put("bootstrap.servers", "localhost:9092")
    kafkaProps.put("client.id", UUID.randomUUID().toString)
    kafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    kafkaProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    val kafkaSink = sc.broadcast(KafkaSink(kafkaProps))

    val inputStream = KafkaUtils.createDirectStream(ssc, PreferConsistent, Subscribe[String, String](Array("test"), kafkaParams))


    val trainingData = inputStream.map(record => {
      val parsedJson = parse(record.value().toString, useBigDecimalForDouble = false)

      val data = ((parsedJson \ "index").asInstanceOf[Double],
        (parsedJson \ "timestamp").asInstanceOf[Double])

      LabeledPoint(data._1, Vectors.dense(Array(data._1, data._2)))
    }).cache()

    val numFeatures = 2
    val model = new StreamingLinearRegressionWithSGD()
      .setInitialWeights(Vectors.zeros(numFeatures))

    // Make a prediction


//    val processedStream = inputStream
//      .flatMap(record => record.value.split(" "))
//      .map(x => (x, 1))
//      .reduceByKey((x, y) => x + y)

//    processedStream.foreachRDD { rdd =>
//      rdd.foreach { message =>
//        kafkaSink.value.send("testResult", "localhost", message.toString())
//      }
//    }
    //processedStream.print(30)

    model.trainOn(trainingData)

    trainingData.foreachRDD(rdd => {
      kafkaSink.value.send("testResult", "localhost", rdd.toString())
    })


    ssc.start()
    ssc.awaitTermination()
  }
}

class KafkaSink(createProducer: () => KafkaProducer[String, String]) extends Serializable {
  lazy val producer = createProducer()
  def send(topic: String, key: String, value: String): Unit = producer.send(new ProducerRecord(topic, key, value))
}


object KafkaSink {
  def apply(config: Properties): KafkaSink = {
    val f = () => {
      val producer = new KafkaProducer[String, String](config)

      sys.addShutdownHook {
        producer.close()
      }

      producer
    }
    new KafkaSink(f)
  }
}
