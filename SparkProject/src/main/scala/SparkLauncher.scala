package main

import org.apache.spark.sql.SparkSession

object SparkLauncher {
  def main(args: Array[String]): Unit = {
    println("STARTING COMPUTATION")
    val logFile = "/usr/local/spark/README.md"
    val spark = SparkSession.builder.appName("Simple Application").getOrCreate()
    val logData = spark.read.textFile(logFile).cache()
    val numAs = logData.filter(line => line.contains("a")).count()
    val numBs = logData.filter(line => line.contains("b")).count()
    println(s"Lines with a: $numAs, Lines with b: $numBs")
    spark.stop()
  }
}
