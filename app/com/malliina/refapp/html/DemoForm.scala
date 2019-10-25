package com.malliina.refapp.html

import play.api.data.{Form, Forms}

object DemoForm {
  val Name = "name"

  val form = Form(Name -> Forms.nonEmptyText)
}
