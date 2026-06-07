def call() {
  sh '''
    cd SpringBootJspApp
    chmod +x mvnw
    ./mvnw clean package -DskipTests
    mkdir -p ${WORKSPACE}/application_build_output
    cp target/SpringBootJsp.war ${WORKSPACE}/application_build_output/
  '''
}
return this
