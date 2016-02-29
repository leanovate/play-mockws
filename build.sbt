name := "play-mockws"

scalaVersion := "2.11.7"

crossScalaVersions := Seq("2.11.7")

scalacOptions ++= Seq("-deprecation", "-feature")

organization := "de.leanovate.play-mockws"

val playVersion = "2.5.0-RC2"

fork := true

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % playVersion % "provided",
  "com.typesafe.play" %% "play-ws" % playVersion % "provided",
  "com.typesafe.play" %% "play-test" % playVersion % "provided"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.6",
  "org.scalacheck" %% "scalacheck" % "1.12.5",
  "org.mockito" % "mockito-core" % "1.10.19"
) map (_ % "test")

Release.settings
