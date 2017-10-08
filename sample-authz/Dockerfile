FROM openjdk:jre-alpine
COPY target/sample-authn.jar application.yml hazelcast.xml /
EXPOSE 80
CMD ["/usr/bin/java", "-jar", "/sample-authz.jar"]
