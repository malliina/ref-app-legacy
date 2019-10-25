package com.malliina.refapp.db

import java.nio.file.{Path, Paths}

import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import play.api.{Configuration, Logger}

case class DatabaseConf(url: String, user: String, pass: String)

object DatabaseConf {
  val userHome = Paths.get(sys.props("user.home"))
  val confFile = userHome.resolve(".refapp/refapp.conf")

  def orFail() = apply().fold(err => throw new Exception(err), identity)

  def fromFile(file: Path = confFile) = {
    val config = Configuration(ConfigFactory.parseFile(confFile.toFile).resolve())
    val databaseConfig = config.get[Configuration]("refapp.db")
    def get(key: String) = databaseConfig.get[String](key)
    DatabaseConf(get("url"), get("user"), get("pass"))
  }

  def apply(): Either[String, DatabaseConf] =
    for {
      user <- read("DB_USER")
      pass <- read("DB_PASSWORD")
      url <- read("DB_URL")
    } yield DatabaseConf(user, pass, url)

  def read(key: String) = sys.env.get(key).filter(_.nonEmpty).toRight(s"Key not found: '$key'.")
}

object HikariConnection {
  private val log = Logger(getClass)

  def apply(conf: DatabaseConf): HikariDataSource = {
    val hikari = new HikariConfig()
    hikari.setDriverClassName("com.mysql.jdbc.Driver")
    hikari.setJdbcUrl(conf.url)
    hikari.setUsername(conf.user)
    hikari.setPassword(conf.pass)
    log info s"Connecting to '${conf.url}'..."
    new HikariDataSource(hikari)
  }
}
