// Comment to get more information during initialization
logLevel := Level.Warn

// https://github.com/scoverage/sbt-scoverage/releases
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.4.3")

// https://github.com/scoverage/sbt-coveralls/releases
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.3.15")

// https://github.com/codacy/sbt-codacy-coverage/releases
addSbtPlugin("com.codacy" % "sbt-codacy-coverage" % "3.0.3")

// https://github.com/scalameta/scalafmt
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.6")

// https://github.com/sbt/sbt-ci-release
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.11.2")

// Scoverage coverage-parser relies on version 1.x
// scala-xml 2.0 is most of the time non breaking
libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
