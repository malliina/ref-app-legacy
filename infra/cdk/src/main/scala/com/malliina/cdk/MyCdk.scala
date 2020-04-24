package com.malliina.cdk

import com.malliina.cdk.StackOne.optionSetting
import software.amazon.awscdk.core.{Construct, Stack, StackProps, App => AWSApp}
import software.amazon.awscdk.services.codebuild._
import software.amazon.awscdk.services.codecommit.Repository
import software.amazon.awscdk.services.codepipeline.actions.{CodeBuildAction, CodeCommitSourceAction}
import software.amazon.awscdk.services.codepipeline.{Artifact, IAction, Pipeline, StageProps}
import software.amazon.awscdk.services.elasticbeanstalk.CfnConfigurationTemplate.ConfigurationOptionSettingProperty
import software.amazon.awscdk.services.elasticbeanstalk.{CfnApplication, CfnConfigurationTemplate, CfnEnvironment}
import software.amazon.awscdk.services.iam._
import software.amazon.awscdk.services.s3.Bucket

import scala.{App => _}

object StackOne {
  def apply(scope: Construct, id: String) = new StackOne(scope, id, None)

  def optionSetting(namespace: String, optionName: String, value: String) =
    ConfigurationOptionSettingProperty
      .builder()
      .namespace(namespace)
      .optionName(optionName)
      .value(value)
      .build()
}

class StackOne(scope: Construct, id: String, props: Option[StackProps])
  extends Stack(scope, id, props.orNull)
  with CDKBuilders {

  val dockerStackName = "64bit Amazon Linux 2018.03 v2.14.1 running Docker 18.09.9-ce"
  val javaStackName = "64bit Amazon Linux 2018.03 v2.10.4 running Java 8"

  val bucket = Bucket.Builder.create(this, "MyCdkBucket").versioned(true).build()
  val beanstalkApp = CfnApplication.Builder
    .create(this, "MyCdkBeanstalk")
    .applicationName("cdk-app")
    .description("Built with CDK in Helsinki")
    .build()
  val serviceRole = Role.Builder
    .create(this, "MyCdkServiceRole")
    .assumedBy(principal("elasticbeanstalk.amazonaws.com"))
    .managedPolicies(
      list(
        ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSElasticBeanstalkEnhancedHealth"),
        ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSElasticBeanstalkService")
      )
    )
    .build()
  val appRole = Role.Builder
    .create(this, "MyCdkAppRole")
    .assumedBy(principal("ec2.amazonaws.com"))
    .managedPolicies(
      list(ManagedPolicy.fromAwsManagedPolicyName("AWSElasticBeanstalkWebTier"))
    )
    .build()
  val instanceProfile = CfnInstanceProfile.Builder
    .create(this, "MyCdkInstanceProfile")
    .path("/")
    .roles(list(appRole.getRoleName))
    .build()
  val configurationTemplate = CfnConfigurationTemplate.Builder
    .create(this, "MyCdkBeanstalkConfigurationTemplate")
    .applicationName(beanstalkApp.getApplicationName)
    .solutionStackName(javaStackName)
    .optionSettings(
      list[AnyRef](
        optionSetting(
          "aws:autoscaling:launchconfiguration",
          "IamInstanceProfile",
          instanceProfile.getRef
        ),
        optionSetting("aws:elasticbeanstalk:environment", "ServiceRole", serviceRole.getRoleName),
        optionSetting("aws:elasticbeanstalk:application:environment", "PORT", "9000"),
        optionSetting(
          "aws:elasticbeanstalk:application:environment",
          "APPLICATION_SECRET",
          "{{resolve:secretsmanager:dev/refapp/secrets:SecretString:appsecret}}"
        )
      )
    )
    .build()
  val beanstalkEnv = CfnEnvironment.Builder
    .create(this, "MyCdkEnv")
    .applicationName(beanstalkApp.getApplicationName)
    .environmentName("cdk-env")
    .templateName(configurationTemplate.getRef)
    .solutionStackName("64bit Amazon Linux 2018.03 v2.10.4 running Java 8")
    .build()
  val buildEnv =
    BuildEnvironment
      .builder()
      .buildImage(LinuxBuildImage.STANDARD_4_0)
      .computeType(ComputeType.SMALL)
      .build()
  val codebuildProject =
    PipelineProject.Builder
      .create(this, "MyCdkBuild")
      .buildSpec(BuildSpec.fromSourceFilename("buildspec-java.yml"))
      .environment(buildEnv)
      .build()
  val repoName = "ref-app"
  val repo = Repository.Builder
    .create(this, "MyCdkCode")
    .repositoryName(repoName)
    .description(s"Repository for $repoName")
    .build()
  val sourceOut = new Artifact()
  val buildOut = new Artifact()
  val pipeline: Pipeline = Pipeline.Builder
    .create(this, "MyCdkPipeline")
    .stages(
      list[StageProps](
        StageProps
          .builder()
          .stageName("Source")
          .actions(
            list[IAction](
              CodeCommitSourceAction.Builder
                .create()
                .actionName("SourceAction")
                .repository(repo)
                .branch("master")
                .output(sourceOut)
                .build()
            )
          )
          .build(),
        StageProps
          .builder()
          .stageName("Build")
          .actions(
            list[IAction](
              CodeBuildAction.Builder
                .create()
                .actionName("BuildAction")
                .project(codebuildProject)
                .input(sourceOut)
                .outputs(list(buildOut))
                .build()
            )
          )
          .build(),
        StageProps
          .builder()
          .stageName("Deploy")
          .actions(
            list[IAction](
              new BeanstalkDeployAction(
                EBDeployActionData(
                  "DeployAction",
                  buildOut,
                  beanstalkApp.getApplicationName,
                  beanstalkEnv.getEnvironmentName
                )
              )
            )
          )
          .build()
      )
    )
    .build()
}

object MyCdk {
  def main(args: Array[String]): Unit = {
    val app = new AWSApp()
    StackOne(app, "hello-cdk")
    val assembly = app.synth()
  }
}
