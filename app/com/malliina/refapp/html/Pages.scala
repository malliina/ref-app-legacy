package com.malliina.refapp.html

import controllers.routes
import play.api.mvc.Call
import scalatags.Text.GenericAttr
import scalatags.Text.all._

import scala.io.Source

object Pages {
  implicit val callAttr = new GenericAttr[Call]
  val reverse = routes.Home
  val empty = modifier()
  val titleTag = tag("title")

  val FeedbackClass = "form-feedback"
  val faviconContent = resourceAsString("resources/favicon-16x16.base64")

  def index = basePage(
    div(id := "ts", `class` := "welcome")("This is it.")
  )

  def formPage(feedback: Option[UserFeedback]) = basePage(
    div(`class` := "form-container")(
      form(`class` := "form", action := reverse.submitForm, method := "POST")(
        h2(`class` := "form-title")("Provide value!"),
        input(`class` := "form-input", `type` := "text", name := DemoForm.Name, placeholder := "Provide text..."),
        button(`class` := "btn form-submit", `type` := "submit")("Save"),
        feedback.map { fb =>
          p(`class` := names(FeedbackClass, if (fb.isSuccess) "form-feedback-success" else "form-feedback-error"))(
            fb.message
          )
        }
      )
    )
  )

  def basePage(bodyContent: Modifier*) = Page(
    html(
      head(
        titleTag("App"),
        link(rel := "shortcut icon", href := s"data:image/png;base64,$faviconContent"),
        link(rel := "stylesheet", href := asset("styles-main.css")),
        link(rel := "shortcut icon", href := asset("img/favicon-256.png")),
        script(src := asset("main.js"))
      ),
      body(
        bodyContent
      )
    )
  )

  def asset(at: String): Call = reverse.versioned(at)

  def resourceAsString(path: String): String = {
    val source = Source.fromResource(path)
    try source.mkString
    finally source.close()
  }

  def names(ns: String*) = ns.filter(_.nonEmpty).mkString(" ")
}
