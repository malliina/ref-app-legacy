package controllers

import com.malliina.refapp.build.BuildInfo
import com.malliina.refapp.html.{DemoForm, Pages, UserFeedback}
import com.malliina.refapp.redis.JedisRedis
import com.zaxxer.hikari.HikariDataSource
import controllers.Assets.Asset
import controllers.Home._
import play.api.data.Form
import play.api.http.Writeable
import play.api.libs.json.Json
import play.api.mvc._

object Home {
  val App = "app"
  val GitHash = "gitHash"
  val NoCache = "no-cache"
  val Version = "version"
  val Welcome = "Welcome"

  val FormFailedMessage = "Fill the form properly."

  def feedback(input: String) = s"You submitted: '$input'."
}

class Home(assets: AssetsBuilder, comps: ControllerComponents, ds: HikariDataSource) extends AbstractController(comps) {
  val redis = JedisRedis().toOption
  val pages = Pages

  def index = okAction(pages.index)

  def form = Action { req =>
    Ok(pages.formPage(UserFeedback(req.flash)))
  }

  def submitForm = Action(parse.form(DemoForm.form, onErrors = (_: Form[String]) => badForm)) { req =>
    Redirect(pages.reverse.form()).flashing(UserFeedback(feedback(req.body)).flash)
  }

  private def badForm =
    BadRequest(pages.formPage(Option(UserFeedback(FormFailedMessage, isSuccess = false))))

  def info = okAction {
    val redisMessage = redis.flatMap { c =>
      c.get("test").map(_ => s"Connected to Redis at '${c.host}'.").toOption
    }
    Json.obj("db" -> ds.getJdbcUrl, "redis" -> redisMessage.toSeq)
  }

  def health = Action {
    Ok(Json.obj(App -> BuildInfo.name, Version -> BuildInfo.version, GitHash -> BuildInfo.gitHash))
      .withHeaders(CACHE_CONTROL -> NoCache)
  }

  def okAction[W: Writeable](w: W) = Action(Ok(w))

  def versioned(path: String, file: Asset) = assets.versioned(path, file)
}
