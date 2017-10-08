FROM openjdk:jre-alpine
COPY target/sample-authn.jar application.yml /
EXPOSE 80
CMD ["/usr/bin/java", "-jar", "/sample-authn.jar"]
