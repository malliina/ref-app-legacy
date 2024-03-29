scalaVersion := "2.12.15"

Seq(
  "com.typesafe.play" % "sbt-plugin" % "2.8.2",
  "com.typesafe.sbt" % "sbt-gzip" % "1.0.2",
  "com.typesafe.sbt" % "sbt-digest" % "1.1.4",
  "com.eed3si9n" % "sbt-buildinfo" % "0.10.0",
  "org.scalameta" % "sbt-scalafmt" % "2.4.3"
) map addSbtPlugin
