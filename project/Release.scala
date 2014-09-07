import com.typesafe.sbt.pgp.PgpKeys
import sbt.Keys._
import sbtrelease.ReleasePlugin.ReleaseKeys._
import xerial.sbt.Sonatype.SonatypeKeys._

object Release {

  val settings =
    xerial.sbt.Sonatype.sonatypeSettings ++
    com.typesafe.sbt.SbtPgp.settings ++
    sbtrelease.ReleasePlugin.releaseSettings ++
    Seq(

      crossBuild := true,

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

      publishArtifactsAction := PgpKeys.publishSigned.value
    )
}
