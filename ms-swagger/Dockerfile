FROM openjdk:jre-alpine
COPY target/ms-swagger.jar application.yml /
EXPOSE 80
CMD ["/usr/bin/java", "-jar", "/ms-swagger.jar"]
