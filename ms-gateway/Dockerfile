FROM openjdk:jre-alpine
RUN apk add --update curl && \
    rm -rf /var/cache/apk/*
COPY target/ms-gateway.jar application.yml /
EXPOSE 80
CMD ["/usr/bin/java", "-Xmx64m", "-jar", "/ms-gateway.jar"]
HEALTHCHECK CMD curl --silent --output /dev/null http://localhost/v1
