package tests

import ch.vorburger.mariadb4j.{DB, DBConfigurationBuilder}
import com.malliina.refapp.db.DatabaseConf
import org.scalatest.{BeforeAndAfterAll, Suite}

trait EmbeddedMySQL extends BeforeAndAfterAll { self: Suite =>
  private val dbConfig =
    DBConfigurationBuilder
      .newBuilder()
      .setDeletingTemporaryBaseAndDataDirsOnShutdown(true)
  lazy val db = DB.newEmbeddedDB(dbConfig.build())
  lazy val conf: DatabaseConf = {
    db.start()
    DatabaseConf(dbConfig.getURL("test"), "root", "", DatabaseConf.MySQLDriver)
  }

  override protected def beforeAll(): Unit = {
    // Starts the database as a side-effect if not already started
    val c = conf
  }

  override protected def afterAll(): Unit = {
    db.stop()
  }
}
