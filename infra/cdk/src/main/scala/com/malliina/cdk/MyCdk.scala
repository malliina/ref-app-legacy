package com.malliina.cdk

import software.amazon.awscdk.core.{Construct, Stack, StackProps, App => AWSApp}
import software.amazon.awscdk.services.elasticbeanstalk.CfnApplication

object BeanstalkApp {
  def apply(scope: Construct, id: String) = new BeanstalkApp(scope, id, None)
}

class BeanstalkApp(scope: Construct, id: String, props: Option[StackProps])
  extends Stack(scope, id, props.orNull)
  with CDKBuilders {
  val beanstalkApp = CfnApplication.Builder
    .create(this, "MyCdkBeanstalk")
    .applicationName("cdk-app")
    .description("Built with CDK in Helsinki")
    .build()
}

object MyCdk {
  def main(args: Array[String]): Unit = {
    val app = new AWSApp()
    val beanstalkApp = BeanstalkApp(app, "hello-cdk")
    val qa = BeanstalkPipeline("ref-app-qa", beanstalkApp.beanstalkApp)
    val assembly = app.synth()
  }
}
