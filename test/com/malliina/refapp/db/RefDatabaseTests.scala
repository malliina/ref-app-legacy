package com.malliina.refapp.db

import com.dimafeng.testcontainers.{ForAllTestContainer, MySQLContainer}
import org.scalatest.FunSuite
import tests.TestConf

class RefDatabaseTests extends FunSuite with ForAllTestContainer {
  override val container = MySQLContainer()
  lazy val refDatabase = RefDatabase.withMigrations(TestConf(container))

  test("make queries") {
    val initial = refDatabase.persons(10)
    assert(initial.isEmpty)
    val kate = PersonInput(Name("Kate"), 47)
    refDatabase.insert(PersonInput(Name("Jack"), 23))
    refDatabase.insert(kate)
    refDatabase.insert(PersonInput(Name("Adele"), 7))
    val after = refDatabase.persons(10)
    assert(after.size === 2)
    assert(!after.exists(_.age < 10))
    assert(after.exists(p => p.age == kate.age && p.name == kate.name))
    val all = refDatabase.persons(0)
    all.foreach { p =>
      refDatabase.remove(p.id)
    }
    assert(refDatabase.persons(0).size === 0)
    refDatabase.close()
  }
}
