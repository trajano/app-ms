Microservice Engine
===================

This uses:
* VertX
* Jersey for REST server
* Jackson for JSON/JAXB processing
* Spring for Dependency Injection
* slf4j logging-api
* logback logging runtime


## Components

### Cluster
The purpose of the cluster was to enable pushing the load across multiple nodes, but for this scenario it didn't make much sense unless there is some real processing that can be done.

### Spring

Spring is used for dependency injection by the users so we can utilize the other stuff that spring has to offer such as scheduling and JPA Repositories.

### Jersey

## Project structure

src/main/java/<basepackage>/Application

Changed to single ApplicationContext 
and require all paths to be singleton context.

The opinionated version will use @Inject

## Quick comparison

### vs. WSO2

Like WSO2 it uses Netty underneath it all which is provided by Vert.X on this implementation.  
This provides the *full JAX-RS server* stack, not just partial support it also provides the *full JAX-RS Client stack* and promotes its use.

This supports Swagger and is ready to use with Postman import support.  WSO2 Swagger does not import properly into Postman.

### vs. Spring-boot

Depending on the implementation used, this uses Netty which is more performant than Tomcat.  This does not provide use the servlet API.