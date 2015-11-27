organization := "com.banno"

name := "api"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.0",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.0-M1",
  "ch.qos.logback" % "logback-classic" % "1.1.3"
)

Revolver.settings
