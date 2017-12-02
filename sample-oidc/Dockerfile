FROM openjdk:jre-alpine
COPY target/sample-oidc.jar application.yml *.json /
EXPOSE 80
CMD ["/usr/bin/java", "-Xmx64m", "-jar", "/sample-oidc.jar"]
HEALTHCHECK CMD wget -qO - http://localhost/.well-known/manifest || exit 1
