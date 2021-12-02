package com.malliina.refapp.db

case class PersonInput(name: Name, age: Int)
case class PersonProfile(id: PersonId, name: Name, age: Int, cars: List[Car])
