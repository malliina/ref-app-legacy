package com.malliina.refapp.db

case class PersonId(id: Int) extends AnyVal
case class CarId(id: Int) extends AnyVal
case class Name(value: String) extends AnyVal
case class Person(id: PersonId, name: Name, age: Int)
case class Car(id: CarId, nickname: String, owner: PersonId)
