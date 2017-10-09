API Gateway
===========

This is an API Gateway implementation using Vert.X to provide similar performance of Netty and Node.JS for asynchronous processing.  This is designed for configuration that is version controlled.

Although this is a Spring Boot application, it does not use the ms-common architecture since it's purpose does not require a JAX-RS stack.

Configuration files are stored in the configuration microservice that provides all the configuration for the system.  

Although spring cloud has a notion of centralized configuration, we're going to forgo that for this scenario.

An token microservice must provide the following API endpoint.


```
response_type
client_id
state
redirect_uri
grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer
grant_type=refresh_token
grant_type=refresh_token
```


```
POST /token
Content-Type: application/x-www-form-urlencoded

grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&access_token=???&client_id=???
grant_type=authorization_code&access_token=???&client_id=???

```

and will return 

```
Content-Type: application/jwt

SOME.JWT.TOKEN
```

The JWT will contain the claims associated with the access token and specifically for the client ID as the `aud`.

## Spring integration

The Gateway does not use the ms-engine itself.  It still uses Spring to provide dependency injection.

### URI processor

/v1/xyz -> local/xyz

The Gateway listens to two ports, one used internally and one for the public.

~~The internal one allows microservices to register themselves into the gateway or internal calls?~~

The gateway is where the microservices are exposed 

The gateway takes a swagger endpoint URI and assigns it to a version group builds the routes from there and assigns it a version identifier.  It also assembles a swagger that collects all of the registered swaggers.  It also uses the tag `unprotected` for paths that do not require the `Bearer` token to be present, otherwise it will implicitly add 

     gateway:
       routes:
       - basePath: /v1
         endpoints:
         - http://sample-ms:8900/
       - basePath: /v2
         endpoints:
         - http://sample-ms:8900/
