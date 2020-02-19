[![Build Status](https://github.com/malliina/ref-app/workflows/Test/badge.svg)](https://github.com/malliina/ref-app/actions)
[![Build Status](https://travis-ci.org/malliina/ref-app.png?branch=master)](https://travis-ci.org/malliina/ref-app)

# Reference Application

A reference application.

## Components

- [Scala](https://www.scala-lang.org/) / [Play](https://www.playframework.com/) backend.
- Database connectivity with [Quill](https://getquill.io/) and migrations with [Flyway](https://flywaydb.org/).
- Server-rendered HTML with [ScalaTags](http://www.lihaoyi.com/scalatags/).
- Webpack-generated frontend assets in [frontend](frontend) including TypeScript and SASS.
- [Selenium](http://www.scalatest.org/user_guide/using_selenium) tests in [SeleniumTests.scala](test/tests/SeleniumTests.scala)
with [testcontainers-scala](https://github.com/testcontainers/testcontainers-scala).
- AWS CloudFormation [templates](infra) for deployment to [AWS Elastic Beanstalk](https://aws.amazon.com/elasticbeanstalk/).

## Running

As prerequisites, install:

- [Java 11](https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html)
- [sbt](https://www.scala-sbt.org/) 1.3.x
- [Node.js](https://nodejs.org/en/) 10.x

### Locally

To run the app:

    sbt run

The app should now run at http://localhost:9000.

A [Play run hook](https://www.playframework.com/documentation/2.8.x/sbtCookbook#Hooking-into-Plays-dev-mode) 
in [NPMRunHook.scala](project/NPMRunHook.scala) launches `npm` when `sbt run` is started, therefore:

- both the backend and frontend are built at the same time
- any built frontend assets are made available to and served by the Scala backend

### With Docker

Optionally, you can choose to run the app in a Docker container. Install Docker on your machine, then run:

    sbt docker:stage
    docker build --tag=hello target/docker/stage
    docker run -p 9000:9000 hello

The app should now run at http://localhost:9000.

## AWS and Deployments

Use the CloudFormation templates in [infra](infra) to deploy:

1. Deploy a VPC with [vpc.cfn.yml](infra/vpc.cfn.yml)
1. Optionally deploy a database with [aurora.cfn.yml](infra/aurora.cfn.yml)
1. Deploy a Beanstalk Application with [beanstalk-app.cfn.yml](infra/beanstalk-app.cfn.yml)
1. Deploy a Beanstalk Environment with [beanstalk-aurora.cfn.yml](infra/beanstalk-aurora.cfn.yml)
1. Deploy a CI pipeline with [codepipeline.cfn.yml](infra/codepipeline.cfn.yml)

Both [Single Container Docker](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/single-container-docker.html) and 
[Java SE](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/java-se-platform.html) platforms are supported. With
Java SE, deployments from source code to production of a medium sized app can be done in less than five minutes.

The templates will provision infrastructure and launch the app. Pushing to master will trigger a redeploy.

## Formatting

Formatting is [configured](.scalafmt.conf) with [Scalafmt](https://scalameta.org/scalafmt/). Use "Format on save" in your IDE.
