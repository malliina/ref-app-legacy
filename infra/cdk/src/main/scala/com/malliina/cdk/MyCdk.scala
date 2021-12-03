package com.malliina.cdk

import software.amazon.awscdk.core.{Construct, Environment, Stack, StackProps, App => AWSApp}

object AppStack {
  val stackProps =
    StackProps
      .builder()
      .env(Environment.builder().account("297686094835").region("eu-west-1").build())
      .build()

  def apply(scope: Construct, id: String, prefix: String) =
    new AppStack(scope, id, Option(stackProps), prefix)
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
