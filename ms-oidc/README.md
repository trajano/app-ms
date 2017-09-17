OpenID Connect
==============

This is a microservice that handles OpenID Connect authentication.  The purpose to to get to the point of having an `access_token` but no claims are provided.

Endpoints:

* `/auth-uri` URI to the authentication endpoint 
* `/auth` starts the authentication process 
* `/jwks` JWKS URI for public keys if JWE is requested
* `/cb` Callback endpoint

### /auth endpoint

* `state` Opaque value used to maintain state between the request and the callback
* `iss` the issuer to be used

The issuer must be registered internally and requires a `.well-known/openid-configuration` endpoint

### /cb endpoint

The callback endpoint will call the token endpoint of the IP and will provide an OAuth 2.0 token specific for the application that the rest of the clients will be using.
