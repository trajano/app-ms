FROM openjdk:jre-alpine
COPY target/sample-oidc.jar application.yml /
EXPOSE 80
CMD ["/usr/bin/java", "-jar", "/sample-oidc.jar"]
HEALTHCHECK CMD wget -qO - http://localhost/.well-known/manifest || exit 1
