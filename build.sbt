name := "play-mockws"

scalaVersion := "2.11.7"

crossScalaVersions := Seq("2.10.6", "2.11.7")

scalacOptions ++= Seq("-deprecation", "-feature")

organization := "de.leanovate.play-mockws"

val playVersion = "2.3.10"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % playVersion % "provided",
  "com.typesafe.play" %% "play-ws" % playVersion % "provided",
  "com.typesafe.play" %% "play-test" % playVersion % "provided" excludeAll ExclusionRule(organization = "org.specs2")
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.4",
  "org.scalacheck" %% "scalacheck" % "1.12.2",
  "org.mockito" % "mockito-core" % "1.10.19"
) map (_ % "test")

Release.settings
