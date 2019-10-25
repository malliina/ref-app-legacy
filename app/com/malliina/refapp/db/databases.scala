package com.malliina.refapp.db

import com.malliina.refapp.db.DatabaseConf.MySQLDriver
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import play.api.{Configuration, Logger}

case class DatabaseConf(url: String, user: String, pass: String, driver: String = MySQLDriver)

object DatabaseConf {
  val MySQLDriver = "com.mysql.jdbc.Driver"

  def orFail() = apply().fold(err => throw new Exception(err), identity)

  def fromConf(conf: Configuration): DatabaseConf = {
    val databaseConfig = conf.get[Configuration]("refapp.db")
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
    hikari.setDriverClassName(conf.driver)
    hikari.setJdbcUrl(conf.url)
    hikari.setUsername(conf.user)
    hikari.setPassword(conf.pass)
    log info s"Connecting to '${conf.url}'..."
    new HikariDataSource(hikari)
  }
}
