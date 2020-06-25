scalaVersion := "2.12.10"

scalacOptions ++= Seq("-unchecked", "-deprecation")

Seq(
  "com.typesafe.play" % "sbt-plugin" % "2.8.2",
  "com.typesafe.sbt" % "sbt-gzip" % "1.0.2",
  "com.typesafe.sbt" % "sbt-digest" % "1.1.4",
  "com.eed3si9n" % "sbt-buildinfo" % "0.9.0",
  "org.scalameta" % "sbt-scalafmt" % "2.3.1",
  "ch.epfl.scala" % "sbt-bloop" % "1.4.2"
) map addSbtPlugin
