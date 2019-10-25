package com.malliina.refapp.db

import java.io.Closeable

import com.zaxxer.hikari.HikariDataSource
import io.getquill.{MysqlJdbcContext, SnakeCase}

object RefDatabase {
  def apply() = new RefDatabase(HikariConnection(DatabaseConf.fromFile()))
}

class RefDatabase(ds: HikariDataSource) extends Closeable {
  lazy val ctx = new MysqlJdbcContext(SnakeCase, ds)
  import ctx._

  def persons(minAge: Int): Seq[PersonProfile] = {
    val q = quote {
      query[Person].leftJoin(query[Car]).on(_.id == _.owner).filter(_._1.age >= lift(minAge))
    }
    run(q).foldLeft(Vector.empty[PersonProfile]) {
      case (acc, (person, car)) =>
        val idx = acc.indexWhere(_.id == person.id)
        if (idx >= 0) {
          val old = acc(idx)
          acc.updated(idx, old.copy(cars = old.cars ++ car.toList))
        } else {
          acc :+ PersonProfile(person.id, person.name, person.age, car.toList)
        }
    }
  }

  def insert(person: PersonInput) = {
    val q = quote {
      query[Person].insert(_.name -> lift(person.name), _.age -> lift(person.age)).returningGenerated(_.id)
    }
    run(q)
  }

  def remove(id: PersonId) = {
    val q = quote {
      query[Person].filter(_.id == lift(id)).delete
    }
    run(q)
  }

  override def close(): Unit = ds.close()
}
