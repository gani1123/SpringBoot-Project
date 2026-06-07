# Jenkins Shared Library Layout

This repository includes a sample `vars/` directory for Jenkins shared library helpers.

## Files

- `vars/gitCheckout.groovy`
- `vars/mavenBuild.groovy`
- `vars/dockerBuild.groovy`
- `vars/dockerPush.groovy`

Each helper can be used from a Jenkins shared library configured as `my-shared-library`:

```groovy
@Library('my-shared-library@main') _

pipeline {
  agent any

  stages {
    stage('Checkout SCM') {
      steps {
        script {
          gitCheckout()
        }
      }
    }

    stage('Build') {
      steps {
        script {
          mavenBuild()
        }
      }
    }

    stage('Docker Build') {
      steps {
        script {
          dockerBuild(imageTag, latestTag)
        }
      }
    }

    stage('Push To ECR') {
      steps {
        script {
          dockerPush(imageTag, latestTag, awsRegion)
        }
      }
    }
  }
}
```

## Local fallback

If the repository is not configured as a Jenkins shared library, the same files are still usable by loading them directly:

```groovy
def gitCheckout = load 'vars/gitCheckout.groovy'
gitCheckout.call()
```

## Jenkins pipeline usage

This repository also contains a standalone `Jenkinsfile` for normal (non-parameterized) Jenkins pipeline jobs.

The pipeline uses environment variables instead of build parameters.

### Required environment variables

- `AWS_ACCOUNT_ID` — AWS account ID for ECR image tagging and ECR/EKS deployment (default: `962800862954`)
- `AWS_REGION` — AWS region for ECR and EKS (default: `us-east-1`)
- `DOCKER_REPOSITORY` — ECR repository and Docker image name (default: `myapp`)
- `EKS_CLUSTER_NAME` — EKS cluster name (default: `dev-eks`)
- `DEPLOYMENT_MANIFEST` — Kubernetes deployment manifest file (default: `Deployment.yaml`)
- `AWS_CREDENTIALS_ID` — Jenkins credential ID for AWS access keys (default: `AWS-cred`)

If `AWS_ACCOUNT_ID` is not set, the pipeline will still build the Docker image locally, but it will skip ECR and EKS stages.

### Example Jenkins setup

1. Create a Jenkins pipeline job.
2. Configure the pipeline to use the repository's `Jenkinsfile`.
3. Set environment variables in the job if values differ from defaults.
4. Ensure the Jenkins AWS credentials ID exists and has the correct AWS access key permissions.

Example environment configuration:

- `AWS_ACCOUNT_ID`: `123456789012`
- `AWS_REGION`: `us-east-1`
- `DOCKER_REPOSITORY`: `springboot-jsp-app`
- `EKS_CLUSTER_NAME`: `dev-eks`
- `AWS_CREDENTIALS_ID`: `AWS-cred`
