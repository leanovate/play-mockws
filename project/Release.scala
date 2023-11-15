import sbt._
import sbt.Keys._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import xerial.sbt.Sonatype.SonatypeKeys._

object Release {

  val settings =
    Seq(
      releaseCrossBuild   := true,
      sonatypeProfileName := "de.leanovate",
      publishMavenStyle   := true,
      publishTo           := sonatypePublishTo.value,
      pomExtra := {
        <url>https://github.com/leanovate/play-mockws</url>
          <licenses>
            <license>
              <name>MIT</name>
              <url>http://opensource.org/licenses/MIT</url>
            </license>
          </licenses>
          <scm>
            <connection>scm:git:github.com/leanovate/play-mockws</connection>
            <developerConnection>scm:git:git@github.com:/leanovate/play-mockws</developerConnection>
            <url>github.com/leanovate/play-mockws</url>
          </scm>
          <developers>
            <developer>
              <id>yanns</id>
              <name>Yann Simon</name>
              <url>http://yanns.github.io/</url>
            </developer>
          </developers>
      },
      releaseProcess := Seq[ReleaseStep](
        checkSnapshotDependencies,
        inquireVersions,
        runClean,
        runTest,
        setReleaseVersion,
        commitReleaseVersion,
        tagRelease,
        ReleaseStep(action = Command.process("publishSigned", _), enableCrossBuild = true),
        setNextVersion,
        commitNextVersion,
        ReleaseStep(action = Command.process("sonatypeReleaseAll", _), enableCrossBuild = true),
        pushChanges
      )
    )
}
