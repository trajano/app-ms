Microservice Application
========================

[![Build Status](https://travis-ci.org/trajano/app-ms.svg?branch=master)](https://travis-ci.org/trajano/app-ms) [![Quality Gate](https://sonarqube.com/api/badges/gate?key=net.trajano.ms.app:app-ms)](https://sonarqube.com/dashboard?id=net.trajano.ms.app:app-ms)

This is an example of a microservice oriented application that a typical enterprise will work with.  It is not meant to be of the scale of NetFlix/Facebook but for more normal loads of a large company where IT is not their primary function.

The technology stack used:

- Vert.X as the reactive framework
- Netty (part of Vert.X) as the web server and client technology
- RESTEasy for JAX-RS
- Spring for depdency injection
- Spring-Data for persistence
- Hazelcast for any caching requirements
- Docker
- Docker-Compose ([local deployment instructions](https://site.trajano.net/app-ms/building.html))
- Etcd for microservice specific key stores
- nginx for static content delivery
- ReactJS for UI
- Google would be the identity provider

## Modules

*ms-common* project that every microservice will be deriving from that will provide the base API
*ms-common-impl* project that every microservice will be deriving from that will provide the base API
*ms-base* project that every microservice will be deriving from to provide Maven setup.

There is a "ms-engine" project that provides the base microservice engine handlers.

There is a "ms-common" project that every microservice will be needing in terms of encryption.  This will provide an opinionated implementation that provides a higher level API and hides most of the details that are provided by the engine.

## Base API

The API will consist of the following API

* SLF4J
* JAX-RS
* Spring Boot AutoConfiguration annotations
* Spring Context annotations
* Swagger annotations
* Jackson Annotations
* CDI 1.2

The ones that are custom to this implementation are, the API will consist of 

* `net.trajano.ms.common.Microservice`