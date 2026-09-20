// Comment to get more information during initialization
logLevel := Level.Warn

// https://github.com/scoverage/sbt-scoverage/releases
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.4.4")

// https://github.com/scalameta/scalafmt
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.6.2")

// https://github.com/sbt/sbt-ci-release
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.12.1")

// Scoverage coverage-parser relies on version 1.x
// scala-xml 2.0 is most of the time non breaking
libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
