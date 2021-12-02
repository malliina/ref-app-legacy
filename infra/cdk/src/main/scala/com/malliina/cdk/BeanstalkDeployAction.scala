package com.malliina.cdk

import java.util.{List => JavaList}

import software.amazon.awscdk.core.Construct
import software.amazon.awscdk.services.codepipeline._
import software.amazon.awscdk.services.events.{IRuleTarget, Rule, RuleProps}
import software.amazon.awscdk.services.iam.ManagedPolicy
import software.amazon.jsii._

import scala.jdk.CollectionConverters.{MapHasAsJava, SeqHasAsJava}

case class EBDeployActionData(
  actionName: String,
  input: Artifact,
  applicationName: String,
  environmentName: String
)

/** Home-made Elastic Beanstalk deploy action because https://github.com/aws/aws-cdk/issues/2516.
  */
class BeanstalkDeployActionProperties(actionName: String, input: Artifact)
  extends ActionProperties {
  override def getActionName: String = actionName
  override def getArtifactBounds: ActionArtifactBounds =
    ActionArtifactBounds.builder().minInputs(1).maxInputs(1).minOutputs(0).maxOutputs(0).build()
  override def getCategory: ActionCategory = ActionCategory.DEPLOY
  override def getProvider: String = "ElasticBeanstalk"
  override def getInputs: JavaList[Artifact] = Seq(input).asJava
}

class BeanstalkDeployAction(conf: EBDeployActionData) extends IAction {
  override def getActionProperties: ActionProperties =
    new BeanstalkDeployActionProperties(conf.actionName, conf.input)

  override def bind(scope: Construct, stage: IStage, options: ActionBindOptions): ActionConfig = {
    options.getBucket.grantRead(options.getRole)
    options.getRole.addManagedPolicy(
      ManagedPolicy.fromAwsManagedPolicyName("AdministratorAccess-AWSElasticBeanstalk")
    )
    ActionConfig
      .builder()
      .configuration(
        JsiiObjectMapper
          .valueToTree(
            Map(
              "ApplicationName" -> conf.applicationName,
              "EnvironmentName" -> conf.environmentName
            ).asJava
          )
      )
      .build()
  }
  override def onStateChange(name: String, target: IRuleTarget, options: RuleProps): Rule = fail
  override def onStateChange(name: String, target: IRuleTarget): Rule = fail
  override def onStateChange(name: String): Rule = fail

  private def fail = throw new Exception("Not supported.")
}
