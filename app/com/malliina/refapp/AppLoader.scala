package com.malliina.refapp

import java.nio.file.Paths

import com.malliina.refapp.db.{DatabaseConf, RefDatabase}
import com.typesafe.config.ConfigFactory
import controllers.{AssetsBuilder, AssetsComponents, Home}
import play.api.ApplicationLoader.Context
import play.api.{BuiltInComponents, BuiltInComponentsFromContext, Configuration}
import play.api.mvc.EssentialFilter
import play.api.routing.Router
import play.filters.HttpFiltersComponents
import play.filters.gzip.GzipFilter
import router.Routes

trait WithComponents[T <: BuiltInComponents] {
  def createComponents(context: Context): T
}

trait AppConf {
  def database: DatabaseConf
}

object AppConf {
  val userHome = Paths.get(sys.props("user.home"))
  val confFile = userHome.resolve(".refapp/refapp.conf")
  val localConf = Configuration(ConfigFactory.parseFile(confFile.toFile))
}

class ProdAppConf(conf: Configuration) extends AppConf {
  override def database: DatabaseConf = DatabaseConf.fromConf(conf)
}

class AppLoader extends LoggingAppLoader[AppComponents] {
  override def createComponents(context: Context): AppComponents =
    new AppComponents(context, conf => new ProdAppConf(conf))
}

object AppComponents {
  def apply(context: Context, resolveConf: Configuration => AppConf): AppComponents =
    new AppComponents(context, resolveConf)
}

class AppComponents(context: Context, resolveConf: Configuration => AppConf)
    extends BuiltInComponentsFromContext(context)
    with HttpFiltersComponents
    with AssetsComponents {
  override val configuration: Configuration = AppConf.localConf.withFallback(context.initialConfiguration)
  val conf = resolveConf(configuration)
  val db = RefDatabase.opt(conf.database)
  override val httpFilters: Seq[EssentialFilter] =
    Seq(AccessLogFilter(executionContext), new GzipFilter(), securityHeadersFilter)
  val as = new AssetsBuilder(httpErrorHandler, assetsMetadata)
  val home = new Home(as, controllerComponents, db.map(_.ds))
  override val router: Router = new Routes(httpErrorHandler, home)
}
