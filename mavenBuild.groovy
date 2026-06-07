def call() {
  sh '''
    cd SpringBootJspApp
    if [ -x ./mvnw ] && [ -f ./.mvn/wrapper/maven-wrapper.properties ]; then
      chmod +x mvnw
      ./mvnw clean package -DskipTests
    else
      mvn clean package -DskipTests
    fi
    mkdir -p ${WORKSPACE}/application_build_output
    cp target/SpringBootJsp.war ${WORKSPACE}/application_build_output/
  '''
}
return this
