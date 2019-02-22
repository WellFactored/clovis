import sbt.addCompilerPlugin

enablePlugins(JavaAppPackaging)

val Http4sVersion          = "0.20.0-M6"
val Specs2Version          = "4.2.0"
val LogbackVersion         = "1.2.3"
val enumeratumCirceVersion = "1.5.20"
val doobieVersion          = "0.6.0"
val circeVersion           = "0.11.1"
val log4catsVersion        = "0.3.0"
val scalacheckVersion      = "1.14.0"
val cirisVersion           = "0.12.1"
val macwireVersion         = "2.3.1"
val refinedVersion         = "0.9.4"

organizationName := "Well-Factored Software Ltd."
startYear := Some(2018)
licenses += ("AGPL-3.0", new URL("https://www.gnu.org/licenses/agpl.html"))

lazy val root = (project in file("."))
  .settings(
    organization := "com.wellfactored",
    name := "clovis",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.8",
    scalacOptions ++= Seq("-Ypartial-unification"),
    libraryDependencies ++= Seq(
      "org.http4s"               %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"               %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s"               %% "http4s-circe"        % Http4sVersion,
      "org.http4s"               %% "http4s-dsl"          % Http4sVersion,
      "org.http4s"               %% "http4s-scala-xml"    % Http4sVersion,
      "is.cir"                   %% "ciris-cats-effect"   % cirisVersion,
      "io.circe"                 %% "circe-generic"       % circeVersion,
      "io.circe"                 %% "circe-literal"       % circeVersion,
      "io.circe"                 %% "circe-parser"        % circeVersion,
      "io.circe"                 %% "circe-refined"       % circeVersion,
      "io.estatico"              %% "newtype"             % "0.4.2",
      "org.scalacheck"           %% "scalacheck"          % scalacheckVersion % "test",
      "io.chrisdavenport"        %% "log4cats-slf4j"      % log4catsVersion,
      "ch.qos.logback"           % "logback-classic"      % LogbackVersion,
      "com.wellfactored"         %% "property-info"       % "1.1.3",
      "com.beachape"             %% "enumeratum-circe"    % enumeratumCirceVersion,
      "org.tpolecat"             %% "doobie-postgres"     % doobieVersion,
      "org.tpolecat"             %% "doobie-scalatest"    % doobieVersion,
      "com.softwaremill.macwire" %% "macros"              % macwireVersion % "provided"
    ),
    addCompilerPlugin("org.spire-math"  %% "kind-projector"     % "0.9.9"),
    addCompilerPlugin("com.olegpy"      %% "better-monadic-for" % "0.2.4"),
    addCompilerPlugin("org.scalamacros" % "paradise"            % "2.1.1" cross CrossVersion.full)
  )

wartremoverErrors ++= Warts.unsafe
wartremoverErrors -= Wart.Any

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Ypartial-unification",
  "-Xfatal-warnings",
  "-Xlint"
)
