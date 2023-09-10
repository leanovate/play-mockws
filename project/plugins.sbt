// Comment to get more information during initialization
logLevel := Level.Warn

// https://github.com/scoverage/sbt-scoverage/releases
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.9")

// https://github.com/scoverage/sbt-coveralls/releases
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.3.11")

// https://github.com/codacy/sbt-codacy-coverage/releases
addSbtPlugin("com.codacy" % "sbt-codacy-coverage" % "1.3.17")

// https://github.com/scalameta/scalafmt
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")

// https://github.com/sbt/sbt-github-actions
addSbtPlugin("com.github.sbt" % "sbt-github-actions" % "0.19.0")
