package com.malliina.refapp.db

import java.io.Closeable

import com.github.jasync.sql.db.ConnectionPoolConfiguration
import com.github.jasync.sql.db.mysql.MySQLConnectionBuilder
import io.getquill.{MysqlJAsyncContext, SnakeCase}
import org.flywaydb.core.Flyway

import scala.concurrent.{ExecutionContext, Future}

object RefDatabase {
  def apply(conf: DatabaseConf, ec: ExecutionContext): RefDatabase = {
    val config = new ConnectionPoolConfiguration(conf.host, 3306, conf.name, conf.user, conf.pass)
    val pool = MySQLConnectionBuilder.createConnectionPool(config)
    val ctx = new MysqlJAsyncContext(SnakeCase, pool)
    new RefDatabase(ctx)(ec)
  }

  def opt(conf: DatabaseConf, ec: ExecutionContext): Option[RefDatabase] =
    if (conf.enabled) Option(withMigrations(conf, ec)) else None

  def withMigrations(conf: DatabaseConf, ec: ExecutionContext): RefDatabase = {
    val flyway = Flyway
      .configure()
      .dataSource(conf.url, conf.user, conf.pass)
      .load()
    flyway.migrate()
    apply(conf, ec)
  }
}

class RefDatabase(val ctx: MysqlJAsyncContext[SnakeCase.type])(implicit ec: ExecutionContext)
  extends Closeable {
  import ctx._

  val personsTable = quote(querySchema[Person]("persons"))
  val carsTable = quote(querySchema[Car]("cars"))

  def persons(minAge: Int): Future[Seq[PersonProfile]] = {
    val q = quote {
      personsTable.leftJoin(carsTable).on(_.id == _.owner).filter(_._1.age >= lift(minAge))
    }
    run(q).map { rows =>
      rows.foldLeft(Vector.empty[PersonProfile]) {
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
  }

  def insert(person: PersonInput): Future[PersonId] = {
    val q = quote {
      personsTable
        .insert(_.name -> lift(person.name), _.age -> lift(person.age))
        .returningGenerated(_.id)
    }
    run(q)
  }

  def remove(id: PersonId): Future[Long] = {
    val q = quote {
      personsTable.filter(_.id == lift(id)).delete
    }
    run(q)
  }

  override def close(): Unit = ctx.close()
}
