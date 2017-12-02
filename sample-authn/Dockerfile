FROM openjdk:jre-alpine
COPY target/sample-authn.jar application.yml /
EXPOSE 80
CMD ["/usr/bin/java", "-Xmx64m", "-jar", "/sample-authn.jar"]
HEALTHCHECK CMD wget -qO - http://localhost/.well-known/manifest || exit 1
