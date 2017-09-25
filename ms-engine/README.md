Microservice Engine
===================

This uses:
* VertX
* Jersey for REST server
* Jackson for JSON/JAXB processing
* Spring for Dependency Injection
* slf4j logging-api
* logback logging runtime


### Cluster
The purpose of the cluster was to enable pushing the load across multiple nodes, but for this scenario it didn't make much sense unless there is some real processing that can be done.

### Spring

Spring is used for dependency injection by the users so we can utilize the other stuff that spring has to offer such as scheduling and JPA Repositories

