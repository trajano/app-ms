FROM nginx:alpine
COPY dist /usr/share/nginx/html
COPY default.conf /etc/nginx/conf.d/
RUN chmod a=rX -R /usr/share/nginx/html /etc/nginx/conf.d/default.conf
HEALTHCHECK CMD wget -qO - http://localhost/ || exit 1
