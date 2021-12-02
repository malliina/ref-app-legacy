import com.typesafe.sbt.SbtNativePackager.autoImport.NativePackagerHelper.contentOf
import com.typesafe.sbt.packager.docker.DockerVersion

import scala.sys.process.Process
import scala.util.Try

val gitHash = settingKey[String]("Git hash")
val dockerHttpPort = settingKey[Int]("HTTP listen port")
val defaultPort = 9000
val Frontend = config("frontend")
val Beanstalk = config("beanstalk")
val testContainersScalaVersion = "0.39.12"
val deploy = taskKey[Unit]("Runs cdk deploy")

inThisBuild(
  Seq(
    organization := "com.malliina",
    version := "1.0.0",
    scalaVersion := "2.13.7"
  )
)

val app = Project("app", file("backend"))
  .enablePlugins(PlayScala, BuildInfoPlugin, DockerPlugin)
  .settings(
    libraryDependencies ++= Seq("core", "hikari").map { m =>
      "org.tpolecat" %% s"doobie-$m" % "1.0.0-RC1"
    } ++ Seq(
      "mysql" % "mysql-connector-java" % "5.1.49",
      "org.flywaydb" % "flyway-core" % "6.5.5",
      "redis.clients" % "jedis" % "3.3.0",
      "com.lihaoyi" %% "scalatags" % "0.11.0",
      specs2 % Test,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
      "org.seleniumhq.selenium" % "selenium-java" % "3.141.59" % Test,
      "com.dimafeng" %% "testcontainers-scala-scalatest" % testContainersScalaVersion % Test,
      "com.dimafeng" %% "testcontainers-scala-mysql" % testContainersScalaVersion % Test
    ),
    dockerVersion := Option(DockerVersion(19, 3, 5, None)),
    dockerBaseImage := "openjdk:11",
    Docker / daemonUser := "daemon",
    dockerRepository := Option("malliina"),
    dockerExposedPorts := Seq(dockerHttpPort.value),
    Universal / javaOptions ++= Seq(
      "-J-Xmx512m",
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
    Frontend / baseDirectory := baseDirectory.value / "frontend",
    Assets / unmanagedResourceDirectories += (Frontend / baseDirectory).value / "dist",
    PlayKeys.playRunHooks += new NPMRunHook(
      (Frontend / baseDirectory).value,
      target.value,
      streams.value.log
    ),
    Frontend / stage := NPMRunHook.stage((Frontend / baseDirectory).value, streams.value.log),
    Docker / stage := (Docker / stage).dependsOn(Frontend / stage).value,
    Universal / stage := (Universal / stage).dependsOn(Frontend / stage).value,
    Beanstalk / stage := (Beanstalk / stage).dependsOn(Universal / stage).value,
    Universal / mappings ++= contentOf("src/universal")
  )

val cdkModules = Seq("s3", "elasticbeanstalk", "codebuild", "codecommit", "codepipeline-actions")

val infra = project
  .in(file("infra") / "cdk")
  .settings(
    libraryDependencies ++= cdkModules.map { module =>
      "software.amazon.awscdk" % module % "1.134.0"
    } ++ Seq(
      "com.typesafe.play" %% "play-json" % "2.9.0",
      "org.scalameta" %% "munit" % "0.7.11" % Test
    ),
    testFrameworks += new TestFramework("munit.Framework"),
    deploy := ProcessIO
      .runProcessSync("cdk deploy", (ThisBuild / baseDirectory).value, streams.value.log)
  )

val root = project.in(file(".")).aggregate(app, infra)

Global / onChangedBuildSource := ReloadOnSourceChanges
