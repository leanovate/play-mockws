name := "play-mockws"

scalaVersion := "2.11.2"

crossScalaVersions := Seq("2.10.4", "2.11.2")

scalacOptions ++= Seq("-deprecation", "-feature")

organization := "de.leanovate.play-mockws"

val playVersion = "2.3.4"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % playVersion % "provided",
  "com.typesafe.play" %% "play-ws" % playVersion % "provided",
  "com.typesafe.play" %% "play-test" % playVersion % "provided" excludeAll ExclusionRule(organization = "org.specs2"),
  "org.mockito" % "mockito-core" % "1.10.10" % "provided"
)

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.2" % "test"

Release.settings

instrumentSettings

ScoverageKeys.highlighting := true
