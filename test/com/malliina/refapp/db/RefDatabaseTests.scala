package com.malliina.refapp.db

import org.scalatest.{BeforeAndAfterAll, FunSuite}

class RefDatabaseTests extends FunSuite with BeforeAndAfterAll {
  val db = RefDatabase()

  test("make queries") {
    val initial = db.persons(10)
    assert(initial.isEmpty)
    val kate = PersonInput(Name("Kate"), 47)
    db.insert(PersonInput(Name("Jack"), 23))
    db.insert(kate)
    db.insert(PersonInput(Name("Adele"), 7))
    val after = db.persons(10)
    assert(after.size === 2)
    assert(!after.exists(_.age < 10))
    assert(after.exists(p => p.age == kate.age && p.name == kate.name))
    val all = db.persons(0)
    all.foreach { p =>
      db.remove(p.id)
    }
    assert(db.persons(0).size === 0)
  }

  override protected def afterAll(): Unit = {
    db.close()
  }
}
