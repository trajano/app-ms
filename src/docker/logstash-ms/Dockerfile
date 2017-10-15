FROM docker.elastic.co/logstash/logstash:5.6.3
RUN rm -f /usr/share/logstash/pipeline/logstash.conf
ADD pipeline/ /usr/share/logstash/pipeline/
ENV XPACK_MONITORING_ENABLED=false
