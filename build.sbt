ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.1"

import indigoplugin.*

addCommandAlias("buildGame", ";compile;fastLinkJS;indigoBuild")
addCommandAlias("runGame", ";compile;fastLinkJS;indigoRun")
addCommandAlias("buildGameFull", ";compile;fullLinkJS;indigoBuildFull")
addCommandAlias("runGameFull", ";compile;fullLinkJS;indigoRunFull")

lazy val zScala =
  (project in file("."))
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .settings(
      name := "ZScala",
      version := "0.0.1",
      scalaVersion := "3.3.1",
      organization := "com.ecoders"
    )
    .settings( // Indigo specific settings
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("Z-scala")
          .withWindowSize(960, 640),
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "indigo" % "0.15.2",
        "io.indigoengine" %%% "indigo-extras" % "0.15.2",
        "io.indigoengine" %%% "indigo-json-circe" % "0.15.2",
      )
    )
