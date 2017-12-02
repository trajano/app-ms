FROM openjdk:jre-alpine
COPY target/ms-gateway.jar application.yml /
EXPOSE 80
CMD ["/usr/bin/java", "-Xmx64m", "-jar", "/ms-gateway.jar"]
HEALTHCHECK CMD wget -qO - http://localhost/v1 || exit 1