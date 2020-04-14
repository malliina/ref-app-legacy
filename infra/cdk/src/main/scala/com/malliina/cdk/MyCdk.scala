package com.malliina.cdk

import software.amazon.awscdk.core.{Construct, Stack, StackProps, App => AWSApp}
import software.amazon.awscdk.services.s3.Bucket
import software.amazon.awscdk.services.elasticbeanstalk.CfnApplication

import scala.{App => _}

object StackOne {
  def apply(scope: Construct, id: String) = new StackOne(scope, id, None)
}

class StackOne(scope: Construct, id: String, props: Option[StackProps]) extends Stack(scope, id, props.orNull) {
  val bucket = Bucket.Builder.create(this, "MyCdkBucket").versioned(true).build()
  val beanstalkApp = CfnApplication.Builder
    .create(this, "MyCdkBeanstalk")
    .applicationName("cdk-app")
    .description("Built with CDK in Helsinki")
    .build()
}

object MyCdk {
  def main(args: Array[String]): Unit = {
    val app = new AWSApp()
    StackOne(app, "hello-cdk")
    val assembly = app.synth()
  }
}
