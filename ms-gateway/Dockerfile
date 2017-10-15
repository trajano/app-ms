FROM openjdk:jre-alpine
RUN apk update && \
  apk add tzdata && \
  cp /usr/share/zoneinfo/America/Toronto /etc/localtime && \
  echo "America/Toronto" > /etc/timezone && \
  apk del tzdata
COPY target/ms-gateway.jar application.yml /
EXPOSE 80
CMD ["/usr/bin/java", "-jar", "/ms-gateway.jar"]
