export PATH=$PATH:/usr/local/spark/bin
spark-submit --class "main.SparkLauncher" --master local[*] /home/osboxes/IdeaProjects/streamer.io/SparkProject/target/scala-2.11/sparkproj_2.11-1.jar