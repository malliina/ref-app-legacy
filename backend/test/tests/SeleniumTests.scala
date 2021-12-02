package tests

import com.malliina.refapp.html.{DemoForm, Pages}
import controllers.Home
import org.openqa.selenium.WebDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import play.api.mvc.Call

/**
  * @see http://www.scalatest.org/user_guide/using_selenium
  */
class SeleniumTests extends BrowserSuite {
  implicit val webDriver: WebDriver = new HtmlUnitDriver
  val host = s"http://localhost:$port"
  val reverse = Pages.reverse

  test("page title is as expected") {
    navigate(reverse.index())
    assert(pageTitle === "App")
  }

  test("form submits with proper feedback") {
    navigate(reverse.form())
    val testInput = "Haha"
    textField(DemoForm.Name).value = testInput
    submit()
    val feedback = find(className(Pages.FeedbackClass))
    assert(feedback.isDefined)
    assert(feedback.get.text === Home.feedback(testInput))
  }

  test("invalid form submission gives error feedback") {
    navigate(reverse.form())
    textField(DemoForm.Name).value = ""
    submit()
    val feedback = find(className(Pages.FeedbackClass))
    assert(feedback.isDefined)
    assert(feedback.get.text === Home.FormFailedMessage)
  }

  def navigate(to: Call): Unit = goTo(s"$host${to.url}")
}
