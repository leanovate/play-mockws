import scala.collection.immutable

ThisBuild / organization := "io.github.hiveteq.play"

// Those are mandatory for the release to Sonatype
ThisBuild / homepage := Some(url("https://github.com/hiveteq/play-mockws-standalone"))
ThisBuild / licenses := List("MIT" -> url("http://opensource.org/licenses/MIT"))
ThisBuild / developers := List(
  Developer(
    "sdudzin",
    "Siarhei Dudzin",
    "",
    url("https://hiveteq.github.io")
  )
)
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
sonatypeRepository                 := "https://s01.oss.sonatype.org/service/local"

// GPG signing
usePgpKeyHex("3B3697C72B4D7CAA458E232D3759F1DA9FA19F17")

val playWsStandaloneVersion = "3.0.2"

fork := true

val playVersion = "3.0.2"

lazy val testDependencies: Seq[ModuleID] = Seq(
  "org.scalatest"     %% "scalatest"       % "3.2.18",
  "org.scalatestplus" %% "scalacheck-1-15" % "3.2.11.0",
  "org.scalacheck"    %% "scalacheck"      % "1.18.0",
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

val scala213 = "2.13.13"
val scala3   = "3.4.1"

ThisBuild / scalaVersion := scala3
ThisBuild / fork         := true
ThisBuild / resolvers += "Typesafe repository".at("https://repo.typesafe.com/typesafe/releases/")

lazy val root = (project in file("."))
  .settings(
    name            := "play-mockws-standalone-root",
    publishArtifact := false
  )
  .aggregate(play30)

lazy val play30 = (project in file("play-mockws-standalone"))
  .settings(
    name               := "play-mockws-standalone",
    crossScalaVersions := Seq(scala213, scala3)
  )
  .settings(
    libraryDependencies ++= Seq(
      "org.playframework" %% "play-ahc-ws-standalone"  % playWsStandaloneVersion % "provided",
      "org.playframework" %% "play-ws-standalone-json" % playWsStandaloneVersion % "provided",
      "org.playframework" %% "play-ws-standalone-xml"  % playWsStandaloneVersion % "provided",
      "org.playframework" %% "play-test"               % playVersion             % "provided"
    ),
    libraryDependencies ++= testDependencies
  )

// Sonatype profile for releases (otherwise it uses the organization name)
sonatypeProfileName := "io.github.hiveteq"
