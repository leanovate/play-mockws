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

// Relocation details for version 3.x
pomExtra := {
  val suffix = CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 13)) => "2.13"
    case Some((3, _))  => "3"
    case _             => ""
  }

  <distributionManagement>
    <relocation>
      <groupId>de.leanovate.play-mockws</groupId>
      <artifactId>play-mockws-2-9_{suffix}</artifactId>
      <version>3.0.0</version>
      <message>The project is shipped for multiple Play versions now.</message>
    </relocation>
  </distributionManagement>
}

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
