package main

import org.apache.spark.sql.SparkSession

class SparkLauncher {
  def main(args: Array[String]): Unit = {
    val logFile = "/usr/local/spark/README.md"
    val spark = SparkSession.builder.appName("Simple Application").getOrCreate()
    val logData = spark.read.textFile(logFile).cache()
    val numAs = logData.filter(line => line.contains("a")).count()
    val numBs = logData.filter(line => line.contains("b")).count()
    println(s"Lines with a: $numAs, Lines with b: $numBs")
    spark.stop()
  }
}
