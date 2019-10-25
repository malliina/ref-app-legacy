scalaVersion := "2.12.10"

scalacOptions ++= Seq("-unchecked", "-deprecation")

Seq(
  "com.typesafe.play" % "sbt-plugin" % "2.7.3",
  "com.typesafe.sbt" % "sbt-gzip" % "1.0.2",
  "com.typesafe.sbt" % "sbt-digest" % "1.1.4",
  "com.typesafe.sbt" % "sbt-less" % "1.1.2",
  "com.eed3si9n" % "sbt-buildinfo" % "0.9.0",
  "org.scalameta" % "sbt-scalafmt" % "2.0.4",
  "ch.epfl.scala" % "sbt-bloop" % "1.3.4"
) map addSbtPlugin
