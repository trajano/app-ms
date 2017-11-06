FROM nginx:alpine
COPY dist /usr/share/nginx/html
COPY common.conf /etc/nginx/conf.d/common.conf
RUN chmod a=rX -R /usr/share/nginx/html /etc/nginx/conf.d/common.conf
