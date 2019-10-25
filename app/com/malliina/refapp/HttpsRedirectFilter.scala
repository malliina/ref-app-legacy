package com.malliina.refapp

import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.libs.streams.Accumulator
import play.api.mvc._

import scala.util.Try

/**
  * @see https://aws.amazon.com/premiumsupport/knowledge-center/redirect-http-https-elb/
  * @see https://support.cloudflare.com/hc/en-us/articles/200170986-How-does-CloudFlare-handle-HTTP-Request-headers-
  */
class HttpsRedirectFilter extends EssentialFilter {

  /** If CloudFlare terminates SSL and ELB is used, then the X-Forwared-Proto header
    * will be "http" regardless of whether the client uses HTTPS or not.
    *
    * So we first check the CloudFlare-specific CF-Visitor header for the scheme
    * and fallback to the X-Forwarded-Proto header iff no CF-Visitor header is present.
    *
    * Only redirects based on headers, because health check endpoints are still
    * HTTP-only.
    *
    * @param next the action
    * @return a possible redirection
    */
  override def apply(next: EssentialAction): EssentialAction = EssentialAction { rh =>
    val shouldRedirect = Proxies.hasPlainHeaders(rh.headers)
    if (shouldRedirect)
      Accumulator.done(Results.MovedPermanently(s"${Proxies.Https}://${rh.host}${rh.uri}"))
    else
      next(rh)
  }
}

/** I was unsuccessful in getting `request.secure` to work in Play,
  * so I rolled my own.
  */
object Proxies {
  val Http = "http"
  val Https = "https"

  val CFVisitor = "CF-Visitor"
  val Scheme = "scheme"

  /** Call me instead of `request.secure`.
    *
    * @param rh request
    * @return true if the requests seems to use SSL, false otherwise
    */
  def isSecure(rh: RequestHeader): Boolean =
    rh.secure || hasSecureHeaders(rh.headers)

  def hasPlainHeaders(headers: Headers): Boolean =
    proto(headers) contains Http

  def hasSecureHeaders(headers: Headers): Boolean =
    proto(headers) contains Https

  /**
    * @param headers request headers
    * @return the raw protocol value, based on `headers` alone
    */
  def proto(headers: Headers): Option[String] =
    cloudFlareProto(headers) orElse xForwardedProto(headers)

  /** Example CF-Visitor value: {"scheme":"https"} or {"scheme":"http"}.
    *
    * @param headers request headers
    * @return the scheme, if any
    */
  def cloudFlareProto(headers: Headers): Option[String] =
    for {
      visitor <- headers.get(CFVisitor)
      json <- Try(Json.parse(visitor)).toOption
      proto <- (json \ Scheme).validate[String].asOpt
    } yield proto

  def xForwardedProto(headers: Headers): Option[String] =
    headers.get(HeaderNames.X_FORWARDED_PROTO)
}
