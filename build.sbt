name := "play-mockws"

scalaVersion := "2.11.4"

crossScalaVersions := Seq("2.10.4", "2.11.4")

scalacOptions ++= Seq("-deprecation", "-feature")

organization := "de.leanovate.play-mockws"

val playVersion = "2.3.6"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % playVersion % "provided",
  "com.typesafe.play" %% "play-ws" % playVersion % "provided",
  "com.typesafe.play" %% "play-test" % playVersion % "provided" excludeAll ExclusionRule(organization = "org.specs2"),
  "org.mockito" % "mockito-core" % "1.10.10" % "provided"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.2",
  "org.scalacheck" %% "scalacheck" % "1.11.6"
) map (_ % "test")

Release.settings

instrumentSettings

ScoverageKeys.highlighting := true
