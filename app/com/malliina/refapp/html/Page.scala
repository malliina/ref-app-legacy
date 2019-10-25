package com.malliina.refapp.html

import java.nio.charset.StandardCharsets

import akka.util.ByteString
import play.api.http.{MimeTypes, Writeable}
import scalatags.Text

/** Helper that enables imports-free usage in Play's `Action`s such as: `Action(Ok(myTags))`.
  *
  * @param html content
  */
case class Page(html: Text.TypedTag[String]) {
  override def toString = html.toString()
}

object Page {
  val DocTypeTag = "<!DOCTYPE html>"

  val typedTagWriteable: Writeable[Text.TypedTag[String]] =
    Writeable(toUtf8, Option(MimeTypes.HTML))

  implicit val html: Writeable[Page] = typedTagWriteable.map(_.html)

  private def toUtf8(tags: Text.TypedTag[String]): ByteString =
    ByteString(DocTypeTag + tags, StandardCharsets.UTF_8.name())
}
