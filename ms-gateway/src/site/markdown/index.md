API Gateway
===========

This is an API Gateway implementation using Vert.X to provide similar performance of Netty and Node.JS for asynchronous processing.  This is designed for configuration that is version controlled.

Although this is a Spring Boot application, it does not use the ms-common architecture since it's purpose does not require a JAX-RS stack.

Configuration files are stored in the configuration microservice that provides all the configuration for the system.  

Although Spring Cloud has a notion of centralized configuration, we're going to forgo that for this scenario.

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

The authorization endpoint must support two grant types and accept the client information from the `Authorization` header.  The gateway does not perform any validation of the client, it simply forwards it to the authorization microservice.  The authorization microservice should not be exposed outside of the gateway.

### authorization_code grant


```
POST /token
Authorization: Basic <base64<clientId:clientSecret>>
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code&code=<access_token>

```

and will return the following.

```
Content-Type: application/json

{
  "access_token": <access token provided>,
  "token_type": "Bearer",
  "id_token": <JWT token containing internal claims>
}
```


### refresh_token grant

```
POST /token
Authorization: Basic <base64<clientId:clientSecret>>
Content-Type: application/x-www-form-urlencoded

grant_type=refresh_token&refresh_token=<refresh_token>

```

and will return 

```
Content-Type: application/json

{
  "access_token": <new access token>,
  "token_type": "Bearer",
  "expires_in": <how till the access_token expires>,
  "refesh_token": <new refresh token>
}
```

### urn:ietf:params:oauth:grant-type:jwt-bearer grant

This grant is not called by the gateway, but by the authentication microservices such as username/password handler or OpenID Connect callback.  It is responsible for transforming a JWT assertion into an internal one that is used by the rest of the application.  It is expected to return an OAuth token without an id_token value. 

```
POST /token
Authorization: Basic <base64<clientId:clientSecret>>
Content-Type: application/x-www-form-urlencoded

grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=<jwt assertion>
```
and will return 

```
Content-Type: application/json

{
  "access_token": <access token>,
  "token_type": "Bearer",
  "expires_in": <how till the access_token expires>,
  "refesh_token": <refresh token>
}
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

         
```
baselines[0].baseline=/v1 
baselines[0].routes[0].from=/hello 
baselines[0].routes[0].to=http://localhost:8280/hello 
baselines[0].routes[1].from=/s 
baselines[0].routes[1].to=http://localhost:8280/s 
baselines[0].routes[2].from=/authn 
baselines[0].routes[2].to=http://localhost:8281/ 
baselines[1].baseline=/v2 
baselines[1].routes[0].from=/hello 
baselines[1].routes[0].to=http://localhost:8280/hello 
baselines[1].routes[1].from=/s 
baselines[1].routes[1].to=http://localhost:8280/s 
baselines[1].routes[2].from=/authn 
baselines[1].routes[2].to=http://localhost:8281/ 
authorizationEndpoint=http://localhost:8282/
```