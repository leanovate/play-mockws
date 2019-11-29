// Comment to get more information during initialization
logLevel := Level.Warn

// https://github.com/scoverage/sbt-scoverage/releases
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.0")

// https://github.com/scoverage/sbt-coveralls/releases
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.2.7")

// https://github.com/codacy/sbt-codacy-coverage/releases
addSbtPlugin("com.codacy" % "sbt-codacy-coverage" % "1.3.15")

// https://github.com/scalameta/scalafmt
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.0.7")
