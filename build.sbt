name := "play-mockws"

scalacOptions ++= Seq("-deprecation", "-feature")

organization := "de.leanovate.play-mockws"

val playVersion = "2.8.1"

fork := true

resolvers += "Typesafe repository".at("https://repo.typesafe.com/typesafe/releases/")

libraryDependencies ++= Seq(
  "com.typesafe.play"      %% "play"                    % playVersion % "provided",
  "com.typesafe.play"      %% "play-ahc-ws"             % playVersion % "provided",
  "com.typesafe.play"      %% "play-test"               % playVersion % "provided",
  "org.scala-lang.modules" %% "scala-collection-compat" % "2.4.1"
)

libraryDependencies ++= Seq(
  "org.scalatest"  %% "scalatest"    % "3.0.8",
  "org.scalacheck" %% "scalacheck"   % "1.14.3",
  "org.mockito"     % "mockito-core" % "3.5.13"
).map(_ % Test)

Release.settings
