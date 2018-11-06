import sbt.addCompilerPlugin

enablePlugins(JavaAppPackaging)

val Http4sVersion = "0.19.0"
val Specs2Version = "4.2.0"
val LogbackVersion = "1.2.3"

val enumeratumVersion = "1.5.13"
lazy val doobieVersion = "0.6.0-RC1"
val circeVersion = "0.10.1"

organizationName := "Well-Factored Software Ltd."
startYear := Some(2018)
licenses += ("AGPL-3.0", new URL("https://www.gnu.org/licenses/agpl.html"))

lazy val root = (project in file("."))
  .settings(
    organization := "com.wellfactored",
    name := "clovis",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.7",
    scalacOptions ++= Seq("-Ypartial-unification"),
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,

      "org.scala-lang.modules" %% "scala-xml" % "1.0.5",

      // For auto-derivation of JSON codecs
      "io.circe" %% "circe-generic" % circeVersion,
      //For string interpolation to JSON model
      "io.circe" %% "circe-literal" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,

      "io.estatico" %% "newtype" % "0.4.2",


      "org.scalacheck" %% "scalacheck" % "1.14.0" % "test",
      "org.scalatest" %% "scalatest" % "3.0.5" % "test",

      "ch.qos.logback" % "logback-classic" % LogbackVersion,

      "com.wellfactored" %% "property-info" % "1.1.1",

      "com.beachape" %% "enumeratum" % enumeratumVersion,
      "com.beachape" %% "enumeratum-circe" % enumeratumVersion,

      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
      "org.tpolecat" %% "doobie-scalatest" % doobieVersion
    ),
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6"),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4"),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4"),
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.8")
  )

wartremoverErrors ++= Warts.unsafe
wartremoverErrors -= Wart.Any

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Ypartial-unification",
  "-Xfatal-warnings",
  "-Xlint"
)
