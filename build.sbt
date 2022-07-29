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
  "org.scala-lang.modules" %% "scala-collection-compat" % "2.8.1"
)

libraryDependencies ++= Seq(
  "org.scalatest"     %% "scalatest"       % "3.2.8",
  "org.scalatestplus" %% "scalacheck-1-15" % "3.2.8.0",
  "org.scalacheck"    %% "scalacheck"      % "1.15.4",
  "org.mockito"        % "mockito-core"    % "3.11.0"
).map(_ % Test)

Release.settings
