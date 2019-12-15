name := "play-mockws"

scalacOptions ++= Seq("-deprecation", "-feature")

organization := "de.leanovate.play-mockws"

val playVersion = "2.7.3"

fork := true

resolvers += "Typesafe repository".at("http://repo.typesafe.com/typesafe/releases/")

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play"        % playVersion % "provided",
  "com.typesafe.play" %% "play-ahc-ws" % playVersion % "provided",
  "com.typesafe.play" %% "play-test"   % playVersion % "provided"
)

libraryDependencies ++= Seq(
  "org.scalatest"  %% "scalatest"   % "3.0.8",
  "org.scalacheck" %% "scalacheck"  % "1.14.3",
  "org.mockito"    % "mockito-core" % "3.2.0"
).map(_ % Test)

Release.settings
