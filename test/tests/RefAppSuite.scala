package tests

import com.malliina.refapp.{AppComponents, WithAppComponents}
import org.scalatest.{FunSuite, TestSuite}
import org.scalatestplus.selenium.WebBrowser

trait RefAppSuite extends OneServerPerSuite2[AppComponents] with WithAppComponents { self: TestSuite =>

}

abstract class BrowserSuite extends FunSuite with RefAppSuite with WebBrowser
