package com.malliina.cdk

import software.amazon.awscdk.services.ec2.ISubnet
import software.amazon.awscdk.services.elasticbeanstalk.CfnConfigurationTemplate.ConfigurationOptionSettingProperty
import software.amazon.awscdk.services.iam.ServicePrincipal

import scala.jdk.CollectionConverters.{MapHasAsJava, SeqHasAsJava}

trait CDKBuilders extends OptionSettings {
  def principal(service: String) = ServicePrincipal.Builder.create(service).build()
  def list[T](xs: T*) = xs.asJava
  def map[T](kvs: (String, T)*) = Map(kvs: _*).asJava
}

sealed abstract class Arch(val name: String) {
  override def toString = name
}

object Arch {
  case object Arm64 extends Arch("arm64")
  case object X86_64 extends Arch("x86_64")
  case object I386 extends Arch("i386")
}

trait Sizes {
  def prefix: String
  def nano: String = render("nano")
  def micro: String = render("micro")
  def small: String = render("small")
  private def render(s: String) = s"$prefix.$s"
}

object t4g extends Sizes {
  override val prefix = "t4g"
}

trait OptionSettings {
  def asgMinSize(size: Int) = optionSetting("aws:autoscaling:asg", "MinSize", s"$size")
  def asgMaxSize(size: Int) = optionSetting("aws:autoscaling:asg", "MaxSize", s"$size")
  def ebEnvironment(optionName: String, value: String) =
    optionSetting("aws:elasticbeanstalk:environment", optionName, value)
  def ebSubnets(subnets: Seq[ISubnet]) =
    ebVpc("Subnets", subnets.map(_.getSubnetId).mkString(","))
  def ebElbSubnets(subnets: Seq[ISubnet]) =
    ebVpc("ELBSubnets", subnets.map(_.getSubnetId).mkString(","))
  def ebVpc(optionName: String, value: String) =
    optionSetting("aws:ec2:vpc", optionName, value)
  def ebDeployment(optionName: String, value: String) =
    optionSetting("aws:elasticbeanstalk:command", optionName, value)
  def ebInstanceType(instance: String) = ebInstanceTypes(Seq(instance))
  def ebInstanceTypes(instanceTypes: Seq[String]) =
    ebInstances("InstanceTypes", instanceTypes.mkString(", "))
  def supportedArchitectures(architectures: Seq[Arch]) =
    ebInstances("SupportedArchitectures", architectures.mkString(", "))
  def ebInstances(optionName: String, value: String) =
    optionSetting("aws:ec2:instances", optionName, value)
  def ebEnvVar(key: String, value: String) =
    optionSetting("aws:elasticbeanstalk:application:environment", key, value)
  def optionSetting(namespace: String, optionName: String, value: String) =
    ConfigurationOptionSettingProperty
      .builder()
      .namespace(namespace)
      .optionName(optionName)
      .value(value)
      .build()
}
