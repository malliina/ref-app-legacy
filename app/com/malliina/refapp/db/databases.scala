package com.malliina.refapp.db

import com.malliina.refapp.db.DatabaseConf.MySQLDriver
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import play.api.{Configuration, Logger}

case class DatabaseConf(enabled: Boolean, url: String, user: String, pass: String, driver: String = MySQLDriver)

object DatabaseConf {
  val MySQLDriver = "com.mysql.jdbc.Driver"

  def fromConf(conf: Configuration): DatabaseConf = {
    val databaseConfig = conf.get[Configuration]("refapp.db")
    def get(key: String) = databaseConfig.get[String](key)
    DatabaseConf(databaseConfig.get[Boolean]("enabled"), get("url"), get("user"), get("pass"))
  }
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
