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
