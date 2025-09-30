import scala.collection.immutable

ThisBuild / scalacOptions ++= Seq("-deprecation", "-feature")

ThisBuild / organization := "de.leanovate.play-mockws"

// Those are mandatory for the release to Sonatype
ThisBuild / homepage := Some(url("https://github.com/leanovate/play-mockws"))
ThisBuild / licenses := List("MIT" -> url("http://opensource.org/licenses/MIT"))

ThisBuild / developers := List(
  Developer(
    "yanns",
    "Yann Simon",
    "",
    url("http://yanns.github.io/")
  )
)

val play29Version = "2.9.9"
val play30Version = "3.0.9"

def play2Dependencies(version: String): Seq[ModuleID] = Seq(
  "com.typesafe.play" %% "play"        % version,
  "com.typesafe.play" %% "play-ahc-ws" % version,
  "com.typesafe.play" %% "play-test"   % version,
).map(_ % Provided)

def play3Dependencies(version: String): Seq[ModuleID] = Seq(
  "org.playframework" %% "play"        % version,
  "org.playframework" %% "play-ahc-ws" % version,
  "org.playframework" %% "play-test"   % version,
).map(_ % Provided)

lazy val testDependencies: Seq[ModuleID] = Seq(
  "org.scalatest"     %% "scalatest"       % "3.2.19",
  "org.scalatestplus" %% "scalacheck-1-18" % "3.2.19.0",
  "org.scalacheck"    %% "scalacheck"      % "1.19.0",
  "org.mockito"        % "mockito-core"    % "5.20.0"
).map(_ % Test)

val scala213 = "2.13.17"
val scala3   = "3.3.6"

ThisBuild / scalaVersion := scala213

ThisBuild / fork := true

ThisBuild / resolvers += "Typesafe repository".at("https://repo.typesafe.com/typesafe/releases/")

lazy val root = (project in file("."))
  .settings(
    name            := "play-mockws",
    publishArtifact := false
  )
  .aggregate(play29, play30)

lazy val play29 = (project in file("play-mockws"))
  .settings(
    name               := "play-mockws-2-9",
    target             := target.value / "play29",
    crossScalaVersions := Seq(scala213, scala3)
  )
  .settings(
    libraryDependencies ++= play2Dependencies(play29Version),
    libraryDependencies ++= testDependencies
  )
  .settings(
    Compile / unmanagedSourceDirectories += (Compile / sourceDirectory).value / "play-2",
    Compile / unmanagedSourceDirectories += (Compile / sourceDirectory).value / "play-2-9",
    Test / unmanagedSourceDirectories += (Test / sourceDirectory).value / "play-2",
    Test / unmanagedSourceDirectories += (Test / sourceDirectory).value / "play-2-9",
  )

lazy val play30 = (project in file("play-mockws"))
  .settings(
    name               := "play-mockws-3-0",
    target             := target.value / "play30",
    crossScalaVersions := Seq(scala213, scala3)
  )
  .settings(
    libraryDependencies ++= play3Dependencies(play30Version),
    libraryDependencies ++= testDependencies
  )
  .settings(
    Compile / unmanagedSourceDirectories += (Compile / sourceDirectory).value / "play-3",
    Compile / unmanagedSourceDirectories += (Compile / sourceDirectory).value / "play-3-0",
    Test / unmanagedSourceDirectories += (Test / sourceDirectory).value / "play-3",
    Test / unmanagedSourceDirectories += (Test / sourceDirectory).value / "play-3-0",
  )
