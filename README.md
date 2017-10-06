Microservice Application
========================

[![Build Status](https://travis-ci.org/trajano/app-ms.svg?branch=master)](https://travis-ci.org/trajano/app-ms) [![Quality Gate](https://sonarqube.com/api/badges/gate?key=net.trajano.ms.app:app-ms)](https://sonarqube.com/dashboard?id=net.trajano.ms.app:app-ms)

This is an example of a microservice oriented application that a typical enterprise will work with.  It is not meant to be of the scale of NetFlix/Facebook but for more normal loads of a large company where IT is not their primary function.

The technology stack used:

- WSO2 Microservices
  - with Spring
  - with Spring-Data for persistence
- Hazelcast for any caching requirements
- Docker
- Docker-Compose
- Etcd for microservice specific key stores
- nginx for static content delivery
- ReactJS for UI
- Google would be the identity provider

There is a "ms-base" project that every microservice will be deriving from.

There is a "ms-common" project that every microservice will be needing in terms of encryption.
