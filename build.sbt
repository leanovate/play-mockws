name := "play-mockws"

scalacOptions ++= Seq("-deprecation", "-feature")

organization := "de.leanovate.play-mockws"

val playVersion = "2.9.0"

ThisBuild / crossScalaVersions := List("2.13.12", "3.3.1")

fork := true

resolvers += "Typesafe repository".at("https://repo.typesafe.com/typesafe/releases/")

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play"        % playVersion % "provided",
  "com.typesafe.play" %% "play-ahc-ws" % playVersion % "provided",
  "com.typesafe.play" %% "play-test"   % playVersion % "provided",
)

libraryDependencies ++= Seq(
  "org.scalatest"     %% "scalatest"       % "3.2.17",
  "org.scalatestplus" %% "scalacheck-1-16" % "3.2.14.0",
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
