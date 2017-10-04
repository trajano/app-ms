Microservice Engine Common
==========================

This project sets up the common framework for all the microservices and has certain rules.

1. Only one `Application` class.
2. The `Application` class must have a main method that bootstraps the whole engine.
3. `/.well-known/manifest` will hold the manifest files
4. Logback for SLF4j
5. `/.well-known/jwks` will be the jwks endpoint

It provides some common services

* Token provider

The application context is laid out as follows

* Configurations <- this is the root
* Application + Common <- this combines the two