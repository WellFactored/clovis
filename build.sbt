import sbt.addCompilerPlugin

enablePlugins(JavaAppPackaging)

val Http4sVersion          = "0.21.15"
val Specs2Version          = "4.2.0"
val LogbackVersion         = "1.2.3"
val enumeratumCirceVersion = "1.6.1"
val doobieVersion          = "0.9.4"
val log4catsVersion        = "1.1.1"
val circeVersion           = "0.13.0"
val scalacheckVersion      = "1.15.2"
val cirisVersion           = "1.2.1"
val macwireVersion         = "2.3.7"
val scalatestVersion       = "3.2.3"
val refinedVersion         = "0.9.4"

organizationName := "Well-Factored Software Ltd."
startYear := Some(2018)
licenses += ("AGPL-3.0", new URL("https://www.gnu.org/licenses/agpl.html"))

lazy val root = (project in file("."))
  .settings(
    organization := "com.wellfactored",
    name := "clovis",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.3",
    libraryDependencies ++= Seq(
      "org.http4s"               %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"               %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s"               %% "http4s-circe"        % Http4sVersion,
      "org.http4s"               %% "http4s-dsl"          % Http4sVersion,
      "org.http4s"               %% "http4s-scala-xml"    % Http4sVersion,
      "is.cir"                   %% "ciris"               % cirisVersion,
      "io.circe"                 %% "circe-generic"       % circeVersion,
      "io.circe"                 %% "circe-literal"       % circeVersion,
      "io.circe"                 %% "circe-parser"        % circeVersion,
      "io.circe"                 %% "circe-refined"       % circeVersion,
      "io.estatico"              %% "newtype"             % "0.4.4",
      "org.scalacheck"           %% "scalacheck"          % scalacheckVersion % Test,
      "io.chrisdavenport"        %% "log4cats-slf4j"      % log4catsVersion,
      "ch.qos.logback"           % "logback-classic"      % LogbackVersion,
      "com.wellfactored"         %% "property-info"       % "1.1.4",
      "com.beachape"             %% "enumeratum-circe"    % enumeratumCirceVersion,
      "org.tpolecat"             %% "doobie-postgres"     % doobieVersion,
      "org.scalatest"            %% "scalatest"           % scalatestVersion % "test",
      "org.tpolecat"             %% "doobie-scalatest"    % doobieVersion % Test,
      "com.softwaremill.macwire" %% "macros"              % macwireVersion % Provided
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.10.3"),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
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
  "-Xfatal-warnings"
)
