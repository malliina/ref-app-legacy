package tests

import com.malliina.refapp.{AppComponents, AppConf, WithComponents}
import com.malliina.refapp.db.DatabaseConf
import org.scalatest.{FunSuite, Suite, TestSuite}
import org.scalatestplus.selenium.WebBrowser
import play.api.ApplicationLoader

class TestAppConf(val database: DatabaseConf) extends AppConf

/** Launches the server at some port for the duration of the test.
  */
trait RefServerSuite extends OneServerPerSuite2[AppComponents] with EmbeddedMySQL {
  self: TestSuite =>
  override def createComponents(context: ApplicationLoader.Context): AppComponents =
    new AppComponents(context, _ => new TestAppConf(conf))
}

abstract class BrowserSuite extends FunSuite with RefServerSuite with WebBrowser

trait TestComponents extends EmbeddedMySQL with WithComponents[AppComponents] { self: Suite =>
  override def createComponents(context: ApplicationLoader.Context): AppComponents =
    new AppComponents(context, _ => new TestAppConf(conf))
}

/** Launches the app for the duration of the test.
  */
trait RefAppSuite extends FunSuite with OneAppPerSuite2[AppComponents] with TestComponents
