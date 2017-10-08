FROM openjdk:jre-alpine
COPY target/sample-authn.jar application.yml /
EXPOSE 8280
CMD ["/usr/bin/java", "-jar", "/sample-ms.jar"]
