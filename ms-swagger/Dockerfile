FROM openjdk:jre-alpine
COPY target/ms-swagger.jar application.yml /
EXPOSE 80
CMD ["/usr/bin/java", "-Xmx64m", "-jar", "/ms-swagger.jar"]
HEALTHCHECK CMD wget -qO - http://localhost/.well-known/manifest || exit 1
