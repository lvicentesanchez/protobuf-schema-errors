import Dependencies._

ThisBuild / scalaVersion := "2.13.6"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "protobuf-errors",
    resolvers += "confluent" at "https://packages.confluent.io/maven/",
    libraryDependencies += kafkaClient,
    libraryDependencies += protobufSerialiser
  )

Compile / PB.targets := Seq(
  PB.gens.java -> (Compile / sourceManaged).value,
  scalapb.gen(javaConversions = true) -> (Compile / sourceManaged).value
)

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
