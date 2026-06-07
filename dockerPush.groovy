def call(String imageTag, String latestTag, String awsRegion = 'us-east-1') {
  sh """
    aws ecr get-login-password --region ${awsRegion} | docker login --username AWS --password-stdin ${imageTag.split('/')[0]}
    docker push ${imageTag}
    docker push ${latestTag}
  """
}
return this
