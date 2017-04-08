name := """doodlecatcher"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

resolvers += Resolver.bintrayRepo("underscoreio", "training")

libraryDependencies ++= Seq(
  jdbc,
  cache,
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  ws
)

libraryDependencies += "underscoreio" %% "doodle" % "0.7.0"
libraryDependencies += "com.roundeights" %% "hasher" % "1.2.0"

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % _ )