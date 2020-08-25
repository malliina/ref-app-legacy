package com.malliina.cdk

import software.amazon.awscdk.core.{Construct, Stack, StackProps, App => AWSApp}
import software.amazon.awscdk.services.elasticbeanstalk.CfnApplication

object AppStack {
  def apply(scope: Construct, id: String, prefix: String) = new AppStack(scope, id, None, prefix)
}

class AppStack(scope: Construct, id: String, props: Option[StackProps], val prefix: String)
  extends Stack(scope, id, props.orNull) {
  val pipeline = BeanstalkPipeline(this)
}

object MyCdk {
  def main(args: Array[String]): Unit = {
    val app = new AWSApp()

    val qa = AppStack(app, "qa-refapp", "qa")

    val prod = AppStack(app, "prod-refapp", "prod")

    val assembly = app.synth()
  }
}
