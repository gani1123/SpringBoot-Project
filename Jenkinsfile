pipeline {
  agent any

  environment {
    AWS_REGION = 'us-east-1'
    DOCKER_REPOSITORY = 'myapp'
    DEPLOYMENT_MANIFEST = 'Deployment.yaml'
  }

  stages {
    stage('Checkout SCM') {
      steps {
        script {
          try {
            def gitCheckout = load 'vars/gitCheckout.groovy'
            gitCheckout.call()
          } catch (err) {
            echo "Checkout helper unavailable, falling back to checkout scm: ${err}"
            checkout scm
          }
        }
      }
    }

    stage('Build') {
      steps {
        script {
          def mavenBuild = load 'vars/mavenBuild.groovy'
          mavenBuild.call()
        }
      }
    }

    stage('Trivy FS Scan') {
      steps {
        sh '''
          echo 'Running Trivy FS Scan...'
          export TMPDIR=/opt/trivy-temp
          trivy fs --severity HIGH,CRITICAL --exit-code 1 application_build_output/
        '''
      }
    }

    stage('Docker Build') {
      steps {
        script {
          def accountId = env.AWS_ACCOUNT_ID?.trim()
          def buildSuffix = env.BUILD_NUMBER ?: 'local'
          def imageTag = "${env.DOCKER_REPOSITORY}:${buildSuffix}"
          def latestTag = "${env.DOCKER_REPOSITORY}:latest"

          if (accountId) {
            imageTag = "${accountId}.dkr.ecr.${env.AWS_REGION}.amazonaws.com/${env.DOCKER_REPOSITORY}:${buildSuffix}"
            latestTag = "${accountId}.dkr.ecr.${env.AWS_REGION}.amazonaws.com/${env.DOCKER_REPOSITORY}:latest"
            echo "Building ECR image ${imageTag}"
          } else {
            echo "AWS_ACCOUNT_ID not configured. Building local image ${imageTag} and skipping ECR/deploy stages."
          }

          def dockerBuild = load 'vars/dockerBuild.groovy'
          dockerBuild.call(imageTag, latestTag)
          env.IMAGE_TAG = imageTag
          env.LATEST_TAG = latestTag
        }
      }
    }

    stage('Trivy Image Scan') {
      steps {
        script {
          sh """
            echo 'Running Trivy Image Scan...'
            export TMPDIR=/opt/trivy-temp
            trivy image --severity HIGH,CRITICAL --exit-code 1 ${env.IMAGE_TAG}
          """
        }
      }
    }

    stage('Push To ECR') {
      when {
        expression { return env.AWS_ACCOUNT_ID?.trim() }
      }
      steps {
        withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials']]) {
          script {
            def accountId = env.AWS_ACCOUNT_ID.trim()
            def imageTag = "${accountId}.dkr.ecr.${env.AWS_REGION}.amazonaws.com/${env.DOCKER_REPOSITORY}:${env.BUILD_NUMBER ?: 'local'}"
            def latestTag = "${accountId}.dkr.ecr.${env.AWS_REGION}.amazonaws.com/${env.DOCKER_REPOSITORY}:latest"
            def dockerPush = load 'vars/dockerPush.groovy'
            dockerPush.call(imageTag, latestTag, env.AWS_REGION)
          }
        }
      }
    }

    stage('Update Manifest') {
      when {
        expression { return env.AWS_ACCOUNT_ID?.trim() }
      }
      steps {
        script {
          def accountId = env.AWS_ACCOUNT_ID.trim()
          def imageTag = "${accountId}.dkr.ecr.${env.AWS_REGION}.amazonaws.com/${env.DOCKER_REPOSITORY}:${env.BUILD_NUMBER ?: 'local'}"
          sh "sed -i 's|IMAGE_PLACEHOLDER|${imageTag}|g' ${env.DEPLOYMENT_MANIFEST}"
        }
      }
    }

    stage('Deploy To EKS') {
      when {
        expression { return env.AWS_ACCOUNT_ID?.trim() }
      }
      steps {
        withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials']]) {
          sh '''
            aws eks update-kubeconfig --region ${AWS_REGION} --name dev-eks
            kubectl apply -f ${DEPLOYMENT_MANIFEST}
            kubectl rollout status deployment/web-app --timeout=300s
          '''
        }
      }
    }
  }

  post {
    always {
      cleanWs()
    }
    success {
      echo '✅ Deployment Successful'
    }
  }
}
