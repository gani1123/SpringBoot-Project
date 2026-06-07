def call(String imageTag, String latestTag) {
  sh """
    docker build -t ${imageTag} -t ${latestTag} .
  """
}
return this
