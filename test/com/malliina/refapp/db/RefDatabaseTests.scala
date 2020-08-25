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

  test("make queries") {
    val initial = await(refDatabase.persons(10))
    assert(initial.isEmpty)
    val kate = PersonInput(Name("Kate"), 47)
    val seq = for {
      _ <- refDatabase.insert(PersonInput(Name("Jack"), 23))
      _ <- refDatabase.insert(kate)
      _ <- refDatabase.insert(PersonInput(Name("Adele"), 7))
      after <- refDatabase.persons(10)
    } yield after

    val after = await(seq)
    assert(after.size === 2)
    assert(!after.exists(_.age < 10))
    assert(after.exists(p => p.age == kate.age && p.name == kate.name))
    val all = await(refDatabase.persons(0))
    all.foreach { p =>
      refDatabase.remove(p.id)
    }
    assert(await(refDatabase.persons(0)).size === 0)
    refDatabase.close()
  }
  def await[T](f: Future[T]): T = Await.result(f, 10.seconds)
}
