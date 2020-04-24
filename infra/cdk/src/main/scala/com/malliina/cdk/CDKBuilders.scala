package com.malliina.cdk

import software.amazon.awscdk.services.iam.ServicePrincipal

import scala.jdk.CollectionConverters.{MapHasAsJava, SeqHasAsJava}

trait CDKBuilders {
  def principal(service: String) = ServicePrincipal.Builder.create(service).build()
  def list[T](xs: T*) = xs.asJava
  def map[T](kvs: (String, T)*) = Map(kvs: _*).asJava
}
