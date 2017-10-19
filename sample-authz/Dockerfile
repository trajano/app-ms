FROM openjdk:jre-alpine
COPY target/sample-authz.jar application.yml clients*.json /
EXPOSE 80
CMD ["/usr/bin/java", "-jar", "/sample-authz.jar"]
HEALTHCHECK CMD wget -qO - http://localhost/.well-known/manifest || exit 1
