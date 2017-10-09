FROM openjdk:jre-alpine
COPY target/sample-gw.jar application.yml /
EXPOSE 80
CMD ["/usr/bin/java", "-jar", "/sample-gw.jar"]
