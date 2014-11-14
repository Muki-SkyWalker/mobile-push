import com.mle.sbtutils.{SbtProjects, SbtUtils}
import sbt.Keys._
import sbt._

/**
 * A scala build file template.
 */
object PushBuild extends Build {
  lazy val mobileProject = SbtProjects.mavenPublishProject("mobile-push").settings(projectSettings: _*)

  lazy val projectSettings = Seq(
    SbtUtils.gitUserName := "malliina",
    SbtUtils.developerName := "Michael Skogberg",
    version := "0.1.1",
    scalaVersion := "2.11.4",
    fork in Test := true,
    libraryDependencies ++= Seq(
      "com.github.malliina" %% "util" % "1.5.0",
      "com.typesafe.play" %% "play-json" % "2.3.5",
      "com.ning" % "async-http-client" % "1.8.13"),
    libraryDependencies := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, scalaMajor)) if scalaMajor >= 11 =>
          libraryDependencies.value :+ "org.scala-lang.modules" %% "scala-xml" % "1.0.1"
        case _ =>
          libraryDependencies.value
      }
    }
  )
}