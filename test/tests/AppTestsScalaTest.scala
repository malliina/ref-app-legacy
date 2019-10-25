package tests

import com.malliina.refapp.Proxies._
import com.malliina.refapp.{AppComponents, WithAppComponents}
import org.scalatest.FunSuite
import play.api.http.HeaderNames.X_FORWARDED_PROTO
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._

class AppTestsScalaTest extends FunSuite with OneAppPerSuite2[AppComponents] with WithAppComponents {
  test("can make request") {
    assert(withHeaders() === 200)
  }

  ignore("X-Forwarded-Proto 1") {
    assert(withHeaders(X_FORWARDED_PROTO -> Http) === MOVED_PERMANENTLY)
  }

  test("X-Forwarded-Proto 2") {
    assert(withHeaders(X_FORWARDED_PROTO -> Https) !== MOVED_PERMANENTLY)
  }

  ignore("CF-Visitor 1") {
    val responseStatus = withHeaders(CFVisitor -> Json.stringify(Json.obj(Scheme -> Http)), X_FORWARDED_PROTO -> Https)
    assert(responseStatus === MOVED_PERMANENTLY)
  }

  test("CF-Visitor 2") {
    val responseStatus = withHeaders(CFVisitor -> Json.stringify(Json.obj(Scheme -> Https)), X_FORWARDED_PROTO -> Http)
    assert(responseStatus !== MOVED_PERMANENTLY)
  }

  def withHeaders(headers: (String, String)*) = {
    val result = route(app, FakeRequest(GET, "/").withHeaders(headers: _*)).get
    status(result)
  }
}
