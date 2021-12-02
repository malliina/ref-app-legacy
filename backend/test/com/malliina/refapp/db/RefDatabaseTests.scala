package com.malliina.refapp.db

import com.dimafeng.testcontainers.{ForAllTestContainer, MySQLContainer}
import org.scalatest.funsuite.AnyFunSuiteLike
import tests.TestConf

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

class RefDatabaseTests extends AnyFunSuiteLike with ForAllTestContainer {
  override val container = MySQLContainer()
  implicit val ec = ExecutionContext.Implicits.global
  lazy val refDatabase = RefDatabase.withMigrations(TestConf(container), ec)

  test("make queries") {}

  def await[T](f: Future[T]): T = Await.result(f, 10.seconds)
}
