package com.malliina.cdk

import software.amazon.awscdk.services.codebuild.{BuildEnvironment, BuildSpec, ComputeType, LinuxBuildImage, PipelineProject}
import software.amazon.awscdk.services.codecommit.Repository
import software.amazon.awscdk.services.codepipeline.actions.{CodeBuildAction, CodeCommitSourceAction}
import software.amazon.awscdk.services.codepipeline.{Artifact, IAction, Pipeline, StageProps}
import software.amazon.awscdk.services.elasticbeanstalk.{CfnApplication, CfnConfigurationTemplate, CfnEnvironment}
import software.amazon.awscdk.services.iam.{CfnInstanceProfile, ManagedPolicy, Role}

object BeanstalkPipeline {
  def apply(envName: String, app: CfnApplication): BeanstalkPipeline =
    new BeanstalkPipeline(envName, app)
}

class BeanstalkPipeline(envName: String, app: CfnApplication) extends CDKBuilders {
  val namePrefix = "MyCdk"
  val stack = app.getStack
  val dockerSolutionStackName = "64bit Amazon Linux 2018.03 v2.14.1 running Docker 18.09.9-ce"
  val javaSolutionStackName = "64bit Amazon Linux 2018.03 v2.10.4 running Java 8"
  val solutionStack = javaSolutionStackName

  val branch = "master"

  def makeId(name: String) = s"$namePrefix-$envName-$name"

  // Environment

  val serviceRole = Role.Builder
    .create(stack, makeId("ServiceRole"))
    .assumedBy(principal("elasticbeanstalk.amazonaws.com"))
    .managedPolicies(
      list(
        ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSElasticBeanstalkEnhancedHealth"),
        ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSElasticBeanstalkService")
      )
    )
    .build()
  val appRole = Role.Builder
    .create(stack, makeId("AppRole"))
    .assumedBy(principal("ec2.amazonaws.com"))
    .managedPolicies(
      list(ManagedPolicy.fromAwsManagedPolicyName("AWSElasticBeanstalkWebTier"))
    )
    .build()
  val instanceProfile = CfnInstanceProfile.Builder
    .create(stack, makeId("InstanceProfile"))
    .path("/")
    .roles(list(appRole.getRoleName))
    .build()
  val configurationTemplate = CfnConfigurationTemplate.Builder
    .create(stack, makeId("BeanstalkConfigurationTemplate"))
    .applicationName(app.getApplicationName)
    .solutionStackName(solutionStack)
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
    .create(stack, makeId("Env"))
    .applicationName(app.getApplicationName)
    .environmentName(envName)
    .templateName(configurationTemplate.getRef)
    .solutionStackName(solutionStack)
    .build()

  // Pipeline

  val buildEnv =
    BuildEnvironment
      .builder()
      .buildImage(LinuxBuildImage.STANDARD_4_0)
      .computeType(ComputeType.SMALL)
      .build()
  val buildSpec =
    if (solutionStack == javaSolutionStackName) "buildspec-java.yml" else "buildspec.yml"
  val codebuildProject =
    PipelineProject.Builder
      .create(stack, makeId("Build"))
      .buildSpec(BuildSpec.fromSourceFilename(buildSpec))
      .environment(buildEnv)
      .build()
  val repo = Repository.Builder
    .create(stack, makeId("Code"))
    .repositoryName(makeId("Repo"))
    .description(s"Repository for $envName environment of app ${app.getApplicationName}.")
    .build()
  val sourceOut = new Artifact()
  val buildOut = new Artifact()
  val pipeline: Pipeline = Pipeline.Builder
    .create(stack, makeId("Pipeline"))
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
                .branch(branch)
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
                  app.getApplicationName,
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
