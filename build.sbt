name := "play-mockws"

scalaVersion := "2.12.2"

crossScalaVersions := Seq("2.11.11", "2.12.2")

scalacOptions ++= Seq("-deprecation", "-feature")

organization := "de.leanovate.play-mockws"

val playVersion = "2.6.0"

fork := true

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play"                             % playVersion % "provided",
  "com.typesafe.play" %% "play-ahc-ws"                      % playVersion % "provided",
  "com.typesafe.play" %% "play-test"                        % playVersion % "provided",
  "com.typesafe.play" %% "play-iteratees-reactive-streams"  % "2.6.1"
)

libraryDependencies ++= Seq(
  "org.scalatest"  %% "scalatest"    % "3.0.1",
  "org.scalacheck" %% "scalacheck"   % "1.13.4",
  "org.mockito"    %  "mockito-core" % "2.7.13"
) map (_ % Test)

Release.settings
