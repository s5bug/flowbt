ThisBuild / scalaVersion := "2.13.10"

lazy val core = (project in file("core")).settings(
  organization := "tf.bug",
  name := "flowbt",
  version := "0.1.0",
  scalaVersion := "2.13.10",
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core" % "2.9.0",
    "co.fs2" %% "fs2-core" % "3.3.0",
    "org.gnieh" %% "fs2-data-xml" % "1.6.0",
  ),
)

lazy val cli = (project in file("cli")).settings(
  organization := "tf.bug",
  name := "flowbt",
  version := "0.1.0",
  scalaVersion := "2.13.10",
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core" % "2.9.0",
    "org.typelevel" %% "cats-effect" % "3.4.0",
    "co.fs2" %% "fs2-core" % "3.3.0",
    "com.outr" %% "scribe" % "3.10.4",
    "com.outr" %% "scribe-cats" % "3.10.4",
    "org.gnieh" %% "fs2-data-xml" % "1.6.0",
    "com.monovore" %% "decline" % "2.3.1",
    "com.monovore" %% "decline-effect" % "2.3.1",
  ),
  Compile / mainClass := Some("tf.bug.flowbt.cli.Main"),
)
