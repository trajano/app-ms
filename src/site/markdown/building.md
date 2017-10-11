Building
========

The application uses Maven to do the builds.

To build and (re)start the whole docker-compose setup as quickly as possible, do the following:

    mvn -q -DskipTests=true package && docker-compose rm -fs && docker-compose up --build