package tests

import com.malliina.refapp.Proxies._
import play.api.http.HeaderNames.X_FORWARDED_PROTO
import play.api.test.FakeRequest
import play.api.test.Helpers._

class AppTestsScalaTest extends RefAppSuite {
  test("can make request") {
    assert(withHeaders() === 200)
  }

  ignore("X-Forwarded-Proto 1") {
    assert(withHeaders(X_FORWARDED_PROTO -> Http) === MOVED_PERMANENTLY)
  }

  test("X-Forwarded-Proto 2") {
    assert(withHeaders(X_FORWARDED_PROTO -> Https) !== MOVED_PERMANENTLY)
  }

  def withHeaders(headers: (String, String)*) = {
    val result = route(app, FakeRequest(GET, "/").withHeaders(headers: _*)).get
    status(result)
  }
}
