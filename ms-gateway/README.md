API Gateway
===========

This is an API Gateway implementation using Vert.X to provide similar performance of Netty and Node.JS for asynchronous processing.  This is designed for configuration that is version controlled.

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
```

and will return 

```
Content-Type: application/jwt

SOME.JWT.TOKEN
```

The JWT will contain the claims associated with the access token and specifically for the client ID as the `aud`.