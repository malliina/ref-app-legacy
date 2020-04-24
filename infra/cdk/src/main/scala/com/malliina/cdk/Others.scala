package com.malliina.cdk

import software.amazon.awscdk.core.Stack
import software.amazon.awscdk.services.codecommit.Repository
import software.amazon.awscdk.services.codepipeline.Pipeline
import software.amazon.awscdk.services.events.{EventPattern, IRuleTarget, Rule}
import software.amazon.awscdk.services.events.targets.CodePipeline
import software.amazon.awscdk.services.iam.{Effect, PolicyDocument, PolicyStatement, Role}

trait Others extends CDKBuilders { self: Stack =>
  def tagTarget(pipeline: Pipeline, repo: Repository) = {
    val codepipelineTarget = CodePipeline.Builder
      .create(pipeline)
      .eventRole(
        Role.Builder
          .create(this, "MyCdkCodeEventRole")
          .path("/")
          .assumedBy(principal("events.amazonaws.com"))
          .inlinePolicies(
            map(
              "cwe-pipeline-execution" -> PolicyDocument.Builder
                .create()
                .statements(
                  list(
                    PolicyStatement.Builder
                      .create()
                      .effect(Effect.ALLOW)
                      .actions(list("codepipeline:StartPipelineExecution"))
                      .resources(list(pipeline.getPipelineArn))
                      .build()
                  )
                )
                .build()
            )
          )
          .build()
      )
      .build()
    Rule.Builder
      .create(this, "MyCdkCodeRule")
      .eventPattern(
        EventPattern
          .builder()
          .source(list("aws.codecommit"))
          .detailType(list("CodeCommit Repository State Change"))
          .resources(list(repo.getRepositoryArn))
          .detail(
            map(
              "event" -> list("referenceCreated"),
              "referenceType" -> list("tag"),
              "referenceName" -> list(map("prefix" -> "release-"))
            )
          )
          .build()
      )
      .targets(list[IRuleTarget](codepipelineTarget))
      .build()
  }

}
