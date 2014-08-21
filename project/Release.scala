import com.typesafe.sbt.pgp.PgpKeys._
import sbt._
import sbt.Keys._
import sbtrelease.ReleasePlugin.ReleaseKeys._
import sbtrelease.ReleaseStateTransformations._
import sbtrelease.ReleaseStep
import xerial.sbt.Sonatype.SonatypeKeys._

object Release {

  lazy val publishSignedArtifactsAction = {
    st: State =>
      val extracted = Project.extract(st)
      val ref = extracted.get(thisProjectRef)
      extracted.runAggregated(publishSigned in Global in ref, st)
  }

  val settings =
    xerial.sbt.Sonatype.sonatypeSettings ++
    com.typesafe.sbt.SbtPgp.settings ++
    sbtrelease.ReleasePlugin.releaseSettings ++
    Seq(

      // remove when issue fixed: https://github.com/sbt/sbt-release/issues/70
      crossScalaVersions := Seq(scalaVersion.value),

      profileName := "de.leanovate",
      publishMavenStyle := true,

      pomExtra := {
        <url>https://github.com/leanovate/doby</url>
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
        publishArtifacts.copy(action = publishSignedArtifactsAction),
        setNextVersion,
        commitNextVersion,
        pushChanges
      )
    )
}
