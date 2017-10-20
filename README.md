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

The public facing ones are:

* *ms-common* project that every microservice will be deriving from that will provide the base API
* *ms-base* project that every microservice will be deriving from to provide Maven setup.
* *ms-common-auth* project that extends common to provide things that are needed to implement the token endpoint.

The ones that provide a microservice itself are:
* *ms-gateway* API Gateway
* *ms-swagger* a swagger aggregator
* *ms-resource* An i18n resource provider

The internal support modules are:
* *ms-common-impl* project will provide an opinionated rendition of the API.
* *ms-engine* provides common lower level constructs that help build common and the gateway.
* *ms-engine-manifest* project the .well-known/manifest route
* *ms-engine-spring-jaxrs* provides the JAX-RS with Spring handling route
* *ms-engine-swagger* provides the Swagger generation route

## Base API

The API will consist of the following API

* SLF4J
* JAX-RS
* Spring Boot AutoConfiguration annotations
* Spring Context annotations
* Swagger annotations
* Jackson Annotations
* CDI 1.2
* Annotations 1.2 (for `@PermitAll` and `@RolesRequired` annotations)
* GSON (for simple JSON object building)

The ones that are custom to this implementation are, the API will consist of 

* `net.trajano.ms.Microservice`
* `net.trajano.ms.common.JwtClaimsSetPrincipal`

## JOSE support

The common API will hide the use of JOSE the implementation will perform the necessary validations 
