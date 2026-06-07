pipeline {
  agent any

  environment {
    AWS_ACCOUNT_ID = '962800862954'
    AWS_REGION = 'us-east-1'
    DOCKER_REPOSITORY = 'myapp'
    EKS_CLUSTER_NAME = 'dev-eks'
    DEPLOYMENT_MANIFEST = 'Deployment.yaml'
    AWS_CREDENTIALS_ID = 'AWS-cred'
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

    stage('Validate AWS Credentials') {
      when {
        expression { return env.AWS_ACCOUNT_ID?.trim() }
      }
      steps {
        withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: env.AWS_CREDENTIALS_ID]]) {
          sh '''
            echo "Checking AWS identity..."
            aws sts get-caller-identity

            echo "Checking ECR access..."
            aws ecr describe-repositories --region ${AWS_REGION} || true

            echo "Checking EKS access..."
            aws eks describe-cluster --name ${EKS_CLUSTER_NAME} --region ${AWS_REGION} || true
          '''
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
          def awsRegion = env.AWS_REGION?.trim() ?: 'us-east-1'
          def dockerRepo = env.DOCKER_REPOSITORY?.trim() ?: 'myapp'
          def buildSuffix = env.BUILD_NUMBER ?: 'local'
          def imageTag = "${dockerRepo}:${buildSuffix}"
          def latestTag = "${dockerRepo}:latest"

          if (accountId) {
            imageTag = "${accountId}.dkr.ecr.${awsRegion}.amazonaws.com/${dockerRepo}:${buildSuffix}"
            latestTag = "${accountId}.dkr.ecr.${awsRegion}.amazonaws.com/${dockerRepo}:latest"
            echo "Building ECR image ${imageTag}"
          } else {
            echo "AWS_ACCOUNT_ID not configured. Building local image ${imageTag} and skipping ECR/deploy stages."
          }

          def dockerBuild = load 'vars/dockerBuild.groovy'
          dockerBuild.call(imageTag, latestTag)
          env.IMAGE_TAG = imageTag
          env.LATEST_TAG = latestTag
          env.AWS_REGION = awsRegion
          env.DOCKER_REPOSITORY = dockerRepo
          env.EKS_CLUSTER_NAME = env.EKS_CLUSTER_NAME?.trim() ?: 'dev-eks'
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
          def originalManifest = env.DEPLOYMENT_MANIFEST ?: 'Deployment.yaml'
          def renderedManifest = "${originalManifest}.rendered"
          sh "cp ${originalManifest} ${renderedManifest}"
          sh "sed -i 's|IMAGE_PLACEHOLDER|${imageTag}|g' ${renderedManifest}"
          env.DEPLOYMENT_MANIFEST = renderedManifest
        }
      }
    }

    stage('Deploy To EKS') {
      when {
        expression { return env.AWS_ACCOUNT_ID?.trim() }
      }
      steps {
        script {
          withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: env.AWS_CREDENTIALS_ID]]) {
            sh '''
              aws eks update-kubeconfig --region ${AWS_REGION} --name ${EKS_CLUSTER_NAME}
              kubectl apply -f ${DEPLOYMENT_MANIFEST}
              kubectl rollout status deployment/web-app --timeout=300s
            '''
          }
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
