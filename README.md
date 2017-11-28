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
- Docker-Compose

For the examples:

- VueJS is used for the UI
- Google would be the identity provider
- nginx for static content delivery
- Spring-Data for persistence
- Hazelcast for any caching requirements

More details are available from the [Maven site](https://site.trajano.net/app-ms)
