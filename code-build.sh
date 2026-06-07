#!/bin/bash

#Fail the script if any command fails 
set -e

# Install OpenJdk 17 
echo "Installing OpenJDK 17..." 
apk update 
apk add openjdk17-jdk 
# apk add --no-cache openjdk21-jdk

#Verifv installations 
echo "Java version installed:" 
java -version

#Clean and build the Spring Boot application 
chmod +x SpringBootJsp/mvnw
cd SpringBootJsp 
echo "Building Artifacts" 
./mvnw clean install


#Copy build artifacts to build output dir 
cp target/SpringBootJsp.war ../application_build_output 
#cp../krb5.conf ../application_build_output 
#cp../jaas.conf ../application_ build_output 
#cp../krb5_ticket.sh ../application_build_output 
cd ../application build output 
1s -lha