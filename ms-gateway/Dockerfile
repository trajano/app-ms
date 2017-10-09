FROM openjdk:jre-alpine
COPY target/ms-gateway.jar application.yml /
EXPOSE 80
CMD ["/usr/bin/java", "-jar", "/ms-gateway.jar"]
