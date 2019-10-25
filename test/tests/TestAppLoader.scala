package tests

import play.api.ApplicationLoader.Context
import play.api.test.WithApplicationLoader
import play.api.{ApplicationLoader, Environment, Mode}

class TestAppLoader(loader: ApplicationLoader) extends WithApplicationLoader(loader)

object TestAppLoader {
  def createTestAppContext: Context = {
    val env =
      new Environment(new java.io.File("."), ApplicationLoader.getClass.getClassLoader, Mode.Test)
    Context.create(env)
  }
}
