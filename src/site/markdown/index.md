Microservice Application
========================

[![Build Status](//travis-ci.org/trajano/app-ms.svg?branch=master)](//travis-ci.org/trajano/app-ms) [![Quality Gate](//sonarcloud.io/api/badges/gate?key=net.trajano.ms.app:app-ms)](//sonarcloud.io/dashboard?id=net.trajano.ms.app:app-ms) [![Javadocs](//javadoc.io/badge/net.trajano.ms.app/ms-common.svg)](//javadoc.io/doc/net.trajano.ms.app/ms-common)

This is an example of a microservice oriented application that a typical enterprise will work with.  It is not meant to be of the scale of NetFlix/Facebook but for more normal loads of a large company where IT is not their primary function.

The core technology stack used:

- Vert.X as the reactive framework
- Netty (part of Vert.X) as the web server and client technology
- RESTEasy for JAX-RS
- Jackson for XML/JSON mapping
- GSON for fast JSON processing
- Swagger for REST API documentation
- Spring boot for configuration
- SLF4J for logging
- Spring for depdency injection
- Docker
- Docker-Compose ([local deployment instructions](./building.html))

For the examples:

- VueJS is used for the UI
- Google would be the identity provider
- nginx for static content delivery
- Spring-Data for persistence
- Hazelcast for any caching requirements

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

The API will consist of the following Java EE API

* JAX-RS
* CDI 1.2
* Annotations 1.2 (for `@PermitAll` and `@RolesRequired` annotations)

And non-Java EE APIs

* SLF4J
* Spring Boot AutoConfiguration annotations
* Spring Context annotations
* Swagger annotations
* Jackson Annotations
* GSON (for simple JSON object building)
* [jose.4.j][] for JOSE support.

The ones that are custom to this implementation are, the API consists of the following key classes.

* [`net.trajano.ms.Microservice`](https://static.javadoc.io/net.trajano.ms.app/ms-common/latest/net/trajano/ms/Microservice.html)
* [`net.trajano.ms.core.CryptoOps`](https://static.javadoc.io/net.trajano.ms.app/ms-common/latest/net/trajano/ms/core/CryptoOps.html)
* [`net.trajano.ms.core.JsonOps`](https://static.javadoc.io/net.trajano.ms.app/ms-common/latest/net/trajano/ms/core/JsonOps.html)
* [`net.trajano.ms.core.JwtClaimsSetPrincipal`](https://static.javadoc.io/net.trajano.ms.app/ms-common/latest/net/trajano/ms/core/JwtClaimsSetPrincipal.html)
* [`net.trajano.ms.core.ErrorResponse`](https://static.javadoc.io/net.trajano.ms.app/ms-common/latest/net/trajano/ms/core/ErrorResponse.html)

## JOSE support

Each microservice provides their own JWKS endpoint.  This is to facilitate secure communications between each other which may be overkill as it simply adds an extra encryption layer, but should satisfy most corporate security rules.

[jose.4.j][] was chosen because it had only one runtime dependency (SLF4J) vs Nimbus that had jcip-annotations and json-smart.  Another reason to favor [jose.4.j][] was the smaller JAR size.

[jose.4.j]: https://bitbucket.org/b_c/jose4j/wiki/Home
