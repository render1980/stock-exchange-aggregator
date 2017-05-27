name := "exchange-client"

version := "1.0"

organization := "com.example"

scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-actor_2.12" % "2.5.2",
  "com.typesafe.akka" % "akka-testkit_2.12" % "2.5.2"
)
