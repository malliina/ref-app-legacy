import com.typesafe.sbt.SbtNativePackager.autoImport.NativePackagerHelper.contentOf
import com.typesafe.sbt.packager.docker.DockerVersion

import scala.sys.process.Process
import scala.util.Try

val gitHash = settingKey[String]("Git hash")
val dockerHttpPort = settingKey[Int]("HTTP listen port")
val defaultPort = 9000
val Frontend = config("frontend")
val Beanstalk = config("beanstalk")
val testContainersScalaVersion = "0.35.0"
val deploy = taskKey[Unit]("Runs cdk deploy")

ThisBuild / organization := "com.malliina"
ThisBuild / version := "1.0.0"
ThisBuild / scalaVersion := "2.13.1"

val p = Project("ref-app", file("."))
  .enablePlugins(PlayScala, BuildInfoPlugin, DockerPlugin)
  .settings(
    organization := "com.malliina",
    version := "1.0.0",
    scalaVersion := "2.13.1",
    scalacOptions ++= Seq(
      "-encoding",
      "UTF-8"
    ),
    libraryDependencies ++= Seq(
      "mysql" % "mysql-connector-java" % "5.1.48",
      "io.getquill" %% "quill-jdbc" % "3.5.2",
      "io.getquill" %% "quill-jasync-mysql" % "3.5.2",
      "org.flywaydb" % "flyway-core" % "5.2.4",
      "redis.clients" % "jedis" % "3.2.0",
      "com.lihaoyi" %% "scalatags" % "0.8.4",
      specs2 % Test,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
      "org.seleniumhq.selenium" % "selenium-java" % "3.141.59" % Test,
      "com.dimafeng" %% "testcontainers-scala-scalatest" % testContainersScalaVersion % Test,
      "com.dimafeng" %% "testcontainers-scala-mysql" % testContainersScalaVersion % Test
    ),
    dockerVersion := Option(DockerVersion(19, 3, 5, None)),
    dockerBaseImage := "openjdk:11",
    daemonUser in Docker := "daemon",
    dockerRepository := Option("malliina"),
    dockerExposedPorts := Seq(dockerHttpPort.value),
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    javaOptions in Universal ++= Seq(
      "-J-Xmx256m",
      s"-Dhttp.port=${dockerHttpPort.value}"
    ),
    gitHash := Try(Process("git rev-parse --short HEAD").lineStream.head).toOption
      .orElse(sys.env.get("CODEBUILD_RESOLVED_SOURCE_VERSION").map(_.take(7)))
      .orElse(sys.env.get("CODEBUILD_SOURCE_VERSION").map(_.take(7)))
      .getOrElse("latest"),
    dockerHttpPort := sys.env.get("HTTP_PORT").map(_.toInt).getOrElse(defaultPort),
    buildInfoKeys := Seq[BuildInfoKey](
      name,
      version,
      scalaVersion,
      sbtVersion,
      "gitHash" -> gitHash.value
    ),
    buildInfoPackage := "com.malliina.refapp.build",
    pipelineStages := Seq(digest, gzip),
    baseDirectory in Frontend := baseDirectory.value / "frontend",
    unmanagedResourceDirectories in Assets += (baseDirectory in Frontend).value / "dist",
    PlayKeys.playRunHooks += new NPMRunHook(
      (baseDirectory in Frontend).value,
      target.value,
      streams.value.log
    ),
    stage in Frontend := NPMRunHook.stage((baseDirectory in Frontend).value, streams.value.log),
    stage in Docker := (stage in Docker).dependsOn(stage in Frontend).value,
    stage in Universal := (stage in Universal).dependsOn(stage in Frontend).value,
    stage in Beanstalk := (stage in Beanstalk).dependsOn(stage in Universal).value,
    mappings in Universal ++= contentOf("src/universal")
  )

val cdkModules = Seq("s3", "elasticbeanstalk", "codebuild", "codecommit", "codepipeline-actions")

val infra = project
  .in(file("infra") / "cdk")
  .settings(
    libraryDependencies ++= cdkModules.map { module =>
      "software.amazon.awscdk" % module % "1.32.2"
    } ++ Seq(
      "com.typesafe.play" %% "play-json" % "2.8.1",
      "org.scalameta" %% "munit" % "0.7.2" % Test
    ),
    testFrameworks += new TestFramework("munit.Framework"),
    deploy := ProcessIO
      .runProcessSync("cdk deploy", (baseDirectory in ThisBuild).value, streams.value.log)
  )

val refapp = project.in(file("solution")).aggregate(p, infra)

Global / onChangedBuildSource := ReloadOnSourceChanges
