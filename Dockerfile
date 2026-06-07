FROM tomcat:10.1-jdk17

RUN rm -rf /usr/local/tomcat/webapps/*

COPY application_build_output/SpringBootJsp.war /usr/local/tomcat/webapps/SpringBootJsp.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
