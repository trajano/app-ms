FROM openjdk:jre-alpine
COPY target/sample-authz.jar application.yml hazelcast.xml /
EXPOSE 80
CMD ["/usr/bin/java", "-jar", "/sample-authz.jar"]
