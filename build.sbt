name := "play-mockws"

scalacOptions ++= Seq("-deprecation", "-feature")

organization := "de.leanovate.play-mockws"

// Those are mandatory for the release to Sonatype
homepage := Some(url("https://github.com/leanovate/play-mockws"))
licenses := List("MIT" -> url("http://opensource.org/licenses/MIT"))

developers := List(
  Developer(
    "yanns",
    "Yann Simon",
    "",
    url("http://yanns.github.io/")
  )
)

val playVersion = "2.9.0"

ThisBuild / crossScalaVersions := List("2.13.12", "3.3.1")
ThisBuild / scalaVersion       := crossScalaVersions.value.head

fork := true

resolvers += "Typesafe repository".at("https://repo.typesafe.com/typesafe/releases/")

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play"        % playVersion % "provided",
  "com.typesafe.play" %% "play-ahc-ws" % playVersion % "provided",
  "com.typesafe.play" %% "play-test"   % playVersion % "provided",
)

libraryDependencies ++= Seq(
  "org.scalatest"     %% "scalatest"       % "3.2.17",
  "org.scalatestplus" %% "scalacheck-1-15" % "3.2.11.0",
  "org.scalacheck"    %% "scalacheck"      % "1.17.0",
  "org.mockito"        % "mockito-core"    % "5.7.0"
).map(_ % Test)

// Sonatype profile for releases (otherwise it uses the organization name)
sonatypeProfileName := "de.leanovate"

// sbt github actions config
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("11"))

ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(
  RefPredicate.StartsWith(Ref.Tag("v")),
  RefPredicate.Equals(Ref.Branch("master"))
)

ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    commands = List("ci-release"),
    name = Some("Publish project"),
    env = Map(
      "PGP_PASSPHRASE"    -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET"        -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
    )
  )
)

// code linting
ThisBuild / githubWorkflowBuildPreamble += WorkflowStep.Run(
  commands = List("scripts/validate-code check"),
  name = Some("Lint")
)
