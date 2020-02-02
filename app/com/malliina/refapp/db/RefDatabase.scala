package com.malliina.refapp.db

import java.io.Closeable

import com.zaxxer.hikari.HikariDataSource
import io.getquill.{MysqlJdbcContext, SnakeCase}
import org.flywaydb.core.Flyway

object RefDatabase {
  def apply(conf: DatabaseConf): RefDatabase = new RefDatabase(HikariConnection(conf))

  def opt(conf: DatabaseConf): Option[RefDatabase] = if (conf.enabled) Option(withMigrations(conf)) else None

  def withMigrations(conf: DatabaseConf): RefDatabase = {
    val flyway = Flyway
      .configure()
      .dataSource(conf.url, conf.user, conf.pass)
      .load()
    flyway.migrate()
    apply(conf)
  }
}

class RefDatabase(val ds: HikariDataSource) extends Closeable {
  lazy val ctx = new MysqlJdbcContext(SnakeCase, ds)
  import ctx._

  val personsTable = quote(querySchema[Person]("persons"))
  val carsTable = quote(querySchema[Car]("cars"))

  def persons(minAge: Int): Seq[PersonProfile] = {
    val q = quote {
      personsTable.leftJoin(carsTable).on(_.id == _.owner).filter(_._1.age >= lift(minAge))
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

  def insert(person: PersonInput): PersonId = {
    val q = quote {
      personsTable.insert(_.name -> lift(person.name), _.age -> lift(person.age)).returningGenerated(_.id)
    }
    run(q)
  }

  def remove(id: PersonId): Long = {
    val q = quote {
      personsTable.filter(_.id == lift(id)).delete
    }
    run(q)
  }

  override def close(): Unit = ds.close()
}
