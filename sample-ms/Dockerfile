FROM openjdk:jre-alpine
COPY target/sample-ms.jar application.yml /
EXPOSE 8280
CMD ["/usr/bin/java", "-jar", "/sample-ms.jar"]
