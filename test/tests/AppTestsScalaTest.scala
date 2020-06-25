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

  test("parse jdbc url") {
    val regex = "jdbc:mysql://([0-9a-zA-Z-]+):?[0-9]*/([0-9a-zA-Z-]+)".r
    val m = regex.findFirstMatchIn("jdbc:mysql://dbhost:3306/ok")
    assert(m.isDefined)
    val groups = m.get
    assert(groups.group(1) === "dbhost")
    assert(groups.group(2) === "ok")
  }

  def withHeaders(headers: (String, String)*) = {
    val result = route(app, FakeRequest(GET, "/").withHeaders(headers: _*)).get
    status(result)
  }
}
