FROM openjdk:jre-alpine
COPY target/dependency/ms-gateway.jar application.yml /
EXPOSE 80
CMD ["/usr/bin/java", "-jar", "/ms-gateway.jar"]
