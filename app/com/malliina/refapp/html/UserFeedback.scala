package com.malliina.refapp.html

import com.malliina.refapp.html.UserFeedback.{IsSuccess, Message}
import play.api.mvc.Flash

case class UserFeedback(message: String, isSuccess: Boolean = true) {
  def flash = Flash(Map(Message -> message, IsSuccess -> (if (isSuccess) "true" else "false")))
}

object UserFeedback {
  val Message = "message"
  val IsSuccess = "isSuccess"

  def apply(flash: Flash): Option[UserFeedback] =
    for {
      message <- flash.get(Message)
      isSuccess <- flash.get(IsSuccess)
    } yield UserFeedback(message, isSuccess != "false")
}
