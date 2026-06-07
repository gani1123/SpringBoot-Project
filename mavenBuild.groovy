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
    ARTIFACT=target/SpringBootJsp.war
    if [ -f target/SpringBootJsp.war.original ]; then
      ARTIFACT=target/SpringBootJsp.war.original
    fi
    echo "Using WAR artifact: ${ARTIFACT}"
    cp "${ARTIFACT}" ${WORKSPACE}/application_build_output/
  '''
}
return this
