package com.malliina.refapp

import play.api.Logger
import play.api.mvc.{EssentialAction, EssentialFilter}

import scala.concurrent.ExecutionContext

object AccessLogFilter {
  def apply(ec: ExecutionContext) = new AccessLogFilter()(ec)
}

class AccessLogFilter()(implicit ec: ExecutionContext) extends EssentialFilter {
  private val log = Logger(getClass)

  override def apply(next: EssentialAction): EssentialAction = { rh =>
    log.trace(s"$rh")
    next(rh).map { r =>
      log.trace(s"${r.header.status} $rh")
      r
    }
  }
}
