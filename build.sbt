name := "play-mockws"

scalaVersion := "2.11.2"

scalacOptions += "-deprecation"

organization := "de.leanovate.doby"

val playVersion = "2.3.3"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % playVersion % "provided",
  "com.typesafe.play" %% "play-ws" % playVersion % "provided",
  "com.typesafe.play" %% "play-test" % playVersion % "provided"
)

Release.settings
