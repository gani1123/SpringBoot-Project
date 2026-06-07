FROM tomcat:latest
RUN apt-get update


RUN rm -rf /usr/local/tomcat/webapps/*
COPY ${CI_PROJECT_DIR}/application_build_output/*.war /usr/local/tomcat/webapps/


CMD ["catalina.sh","run"]