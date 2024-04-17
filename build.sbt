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

val play28Version = "2.8.21"
val play29Version = "2.9.2"
val play30Version = "3.0.2"

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
  "org.scalatest"     %% "scalatest"       % "3.2.18",
  "org.scalatestplus" %% "scalacheck-1-15" % "3.2.11.0",
  "org.scalacheck"    %% "scalacheck"      % "1.17.0",
  "org.mockito"        % "mockito-core"    % "5.11.0"
).map(_ % Test)

def scalaCollectionsCompat(scalaVersion: String): immutable.Seq[ModuleID] = {
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, n)) if n == 12 =>
      List("org.scala-lang.modules" %% "scala-collection-compat" % "2.12.0")
    case _ =>
      Nil
  }
}

val scala212 = "2.12.19"
val scala213 = "2.13.13"
val scala3   = "3.3.3"

ThisBuild / scalaVersion := scala213

ThisBuild / fork := true

ThisBuild / resolvers += "Typesafe repository".at("https://repo.typesafe.com/typesafe/releases/")

lazy val root = (project in file("."))
  .settings(
    name            := "play-mockws",
    publishArtifact := false
  )
  .aggregate(play28, play29, play30)

lazy val play28 = (project in file("play-mockws"))
  .settings(
    name               := "play-mockws-2-8",
    target             := target.value / "play28",
    crossScalaVersions := Seq(scala212, scala213)
  )
  .settings(
    libraryDependencies ++= play2Dependencies(play28Version),
    libraryDependencies ++= scalaCollectionsCompat(scalaVersion.value),
    libraryDependencies ++= testDependencies
  )
  .settings(
    Compile / unmanagedSourceDirectories += (Compile / sourceDirectory).value / "play-2",
    Compile / unmanagedSourceDirectories += (Compile / sourceDirectory).value / "play-2-8",
    Test / unmanagedSourceDirectories += (Test / sourceDirectory).value / "play-2",
    Test / unmanagedSourceDirectories += (Test / sourceDirectory).value / "play-2-8",
  )

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

// Sonatype profile for releases (otherwise it uses the organization name)
sonatypeProfileName := "de.leanovate"
