FROM openjdk:jre-alpine
COPY target/sample-authz.jar application.yml /
EXPOSE 80
CMD ["/usr/bin/java", "-jar", "/sample-authz.jar"]
