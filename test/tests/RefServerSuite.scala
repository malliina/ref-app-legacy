package tests

import com.dimafeng.testcontainers.{ForAllTestContainer, MySQLContainer}
import com.malliina.refapp.db.DatabaseConf
import com.malliina.refapp.{AppComponents, AppConf, WithComponents}
import org.scalatest.{FunSuite, Suite, TestSuite}
import org.scalatestplus.selenium.WebBrowser
import play.api.ApplicationLoader

case class TestAppConf(database: DatabaseConf) extends AppConf

object TestConf {
  def apply(container: MySQLContainer): DatabaseConf =
    DatabaseConf(
      true,
      s"${container.jdbcUrl}?useSSL=false",
      "localhost",
      container.databaseName,
      container.username,
      container.password
    )
}

/** Launches the server at some port for the duration of the test.
  *
  * Uses a Docker MySQL container as a test database.
  */
trait RefServerSuite
  extends FunSuite
  with OneServerPerSuite2[AppComponents]
  with ForAllTestContainer {
  self: TestSuite =>
  override val container = MySQLContainer()

  override def createComponents(context: ApplicationLoader.Context): AppComponents = {
    container.start()
    val conf = TestConf(container)
    AppComponents(context, _ => TestAppConf(conf))
  }
}

abstract class BrowserSuite extends FunSuite with RefServerSuite with WebBrowser

trait TestComponents extends FunSuite with WithComponents[AppComponents] with ForAllTestContainer {
  self: Suite =>
  override val container = MySQLContainer()

  override def createComponents(context: ApplicationLoader.Context): AppComponents = {
    val conf = TestConf(container)
    AppComponents(context, _ => TestAppConf(conf))
  }
}

/** Launches the app for the duration of the test.
  */
trait RefAppSuite extends FunSuite with OneAppPerSuite2[AppComponents] with TestComponents
