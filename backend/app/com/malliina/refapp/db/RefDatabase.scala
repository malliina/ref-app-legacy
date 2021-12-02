package com.malliina.refapp.db

import org.flywaydb.core.Flyway

import java.io.Closeable
import scala.concurrent.ExecutionContext

object RefDatabase {
  def opt(conf: DatabaseConf, ec: ExecutionContext): Option[RefDatabase] =
    if (conf.enabled) Option(withMigrations(conf, ec)) else None

  def withMigrations(conf: DatabaseConf, ec: ExecutionContext): RefDatabase = {
    val flyway = Flyway
      .configure()
      .dataSource(conf.url, conf.user, conf.pass)
      .load()
    flyway.migrate()
    new RefDatabase
  }
}

class RefDatabase extends Closeable {
  override def close(): Unit = ()
}
