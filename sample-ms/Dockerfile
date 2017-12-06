FROM openjdk:jre-alpine
COPY target/sample-ms.jar application.yml /
EXPOSE 80
CMD ["/usr/bin/java", "-Xmx64m", "-jar", "/sample-ms.jar"]
HEALTHCHECK --start-period=60s CMD wget -qO - http://localhost/.well-known/manifest || exit 1