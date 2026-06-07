pipeline {
  parameters {
    string(name: 'AWS_ACCOUNT_ID', defaultValue: '', description: 'AWS account ID for ECR image tagging and ECR/EKS deployment')
    string(name: 'AWS_REGION', defaultValue: 'us-east-1', description: 'AWS region for ECR and EKS')
    string(name: 'DOCKER_REPOSITORY', defaultValue: 'myapp', description: 'ECR repository and Docker image name')
    string(name: 'EKS_CLUSTER_NAME', defaultValue: 'dev-eks', description: 'EKS cluster name')
    string(name: 'DEPLOYMENT_MANIFEST', defaultValue: 'Deployment.yaml', description: 'Kubernetes deployment manifest file')
    string(name: 'AWS_CREDENTIALS_ID', defaultValue: 'aws-credentials', description: 'Jenkins credential ID for AWS access keys')
  }

  agent any

  environment {
    AWS_ACCOUNT_ID = "${params.AWS_ACCOUNT_ID}"
    AWS_REGION = "${params.AWS_REGION}"
    DOCKER_REPOSITORY = "${params.DOCKER_REPOSITORY}"
    EKS_CLUSTER_NAME = "${params.EKS_CLUSTER_NAME}"
    DEPLOYMENT_MANIFEST = "${params.DEPLOYMENT_MANIFEST}"
    AWS_CREDENTIALS_ID = "${params.AWS_CREDENTIALS_ID}"
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

    stage('Validate AWS Deployment Settings') {
      when {
        expression { return env.AWS_ACCOUNT_ID?.trim() }
      }
      steps {
        script {
          def credentialsId = env.AWS_CREDENTIALS_ID?.trim()
          def accountId = env.AWS_ACCOUNT_ID?.trim()
          def clusterName = env.EKS_CLUSTER_NAME?.trim()
          if (!accountId) {
            error('AWS_ACCOUNT_ID is required for ECR/EKS deployment')
          }
          if (!credentialsId) {
            error('AWS_CREDENTIALS_ID is required for ECR/EKS deployment')
          }
          if (!clusterName) {
            error('EKS_CLUSTER_NAME is required for EKS deployment')
          }
          echo "AWS deployment settings validated: accountId=${accountId}, region=${env.AWS_REGION}, cluster=${clusterName}, credentialsId=${credentialsId}"
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
        withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: env.AWS_CREDENTIALS_ID]]) {
          script {
            def imageTag = env.IMAGE_TAG ?: error('IMAGE_TAG must be set for ECR push')
            def latestTag = env.LATEST_TAG ?: error('LATEST_TAG must be set for ECR push')
            sh """
              aws ecr describe-repositories --repository-names ${env.DOCKER_REPOSITORY} --region ${env.AWS_REGION} || \
                aws ecr create-repository --repository-name ${env.DOCKER_REPOSITORY} --region ${env.AWS_REGION}
            """
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
          def imageTag = env.IMAGE_TAG ?: error('IMAGE_TAG must be set for manifest update')
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
            aws eks update-kubeconfig --region ${AWS_REGION} --name ${EKS_CLUSTER_NAME}
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
