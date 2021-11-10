import sbt._

object Dependencies {
  lazy val kafkaClient = "org.apache.kafka" % "kafka-clients" % "3.0.0"
  lazy val protobufSerialiser =
    "io.confluent" % "kafka-protobuf-serializer" % "6.2.1"
}
