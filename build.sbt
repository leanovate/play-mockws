name := "play-mockws"

scalaVersion := "2.11.12"

crossScalaVersions := Seq("2.11.12")

scalacOptions ++= Seq("-deprecation", "-feature")

organization := "de.leanovate.play-mockws"

val playVersion = "2.5.12"

fork := true

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % playVersion % "provided",
  "com.typesafe.play" %% "play-ws" % playVersion % "provided",
  "com.typesafe.play" %% "play-test" % playVersion % "provided"
)

libraryDependencies ++= Seq(
  "org.scalatest"  %% "scalatest"    % "3.0.5",
  "org.scalacheck" %% "scalacheck"   % "1.13.5",
  "org.mockito"    %  "mockito-core" % "2.19.0"
) map (_ % Test)

Release.settings
