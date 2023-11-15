name := "play-mockws"

scalacOptions ++= Seq("-deprecation", "-feature")

organization := "de.leanovate.play-mockws"

val playVersion = "3.0.0"

ThisBuild / crossScalaVersions := List("2.13.1", "3.3.1")

fork := true

resolvers += "Typesafe repository".at("https://repo.typesafe.com/typesafe/releases/")

libraryDependencies ++= Seq(
  "org.playframework" %% "play"        % playVersion % "provided",
  "org.playframework" %% "play-ahc-ws" % playVersion % "provided",
  "org.playframework" %% "play-test"   % playVersion % "provided",
)

libraryDependencies ++= Seq(
  "org.scalatest"     %% "scalatest"       % "3.2.17",
  "org.scalatestplus" %% "scalacheck-1-15" % "3.2.11.0",
  "org.scalacheck"    %% "scalacheck"      % "1.15.4",
  "org.mockito"        % "mockito-core"    % "3.11.0"
).map(_ % Test)

Release.settings

ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("11"))

// code linting
ThisBuild / githubWorkflowBuildPreamble += WorkflowStep.Run(
  commands = List("scripts/validate-code check"),
  name = Some("Lint")
)
