export PATH=$PATH:/usr/local/spark/bin
spark-submit --class "main.SparkLauncher" --master local[*] /home/osboxes/IdeaProjects/streamer.io/SparkProject/target/scala-2.12/sparkproj_2.12-0.1-SNAPSHOT.jar