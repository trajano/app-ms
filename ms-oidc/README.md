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

The cb endpoint will call another microservice to generate the token if the issuer subject data is valid.

```
POST /token
Content-Type: application/x-www-form-urlencoded

grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=id_token_from_IP&state=optional

HTTP/1.1 200 OK
Content-Type: application/json;charset=UTF-8
Cache-Control: no-store
Pragma: no-cache

{
  "access_token":"2YotnFZFEjr1zCsicMWpAA",
  "token_type":"Bearer",
  "expires_in":3600,
  "refresh_token":"tGzv3JOkF0XG5Qx2TlKWIA"
}
```



  error
         REQUIRED.  A single ASCII [USASCII] error code from the
         following:

         invalid_request
               The request is missing a required parameter, includes an
               invalid parameter value, includes a parameter more than
               once, or is otherwise malformed.

         unauthorized_client
               The client is not authorized to request an access token
               using this method.

         access_denied
               The resource owner or authorization server denied the
               request.

         unsupported_response_type
               The authorization server does not support obtaining an
               access token using this method.

         invalid_scope
               The requested scope is invalid, unknown, or malformed.









Hardt                        Standards Track                   [Page 36]

 
RFC 6749                        OAuth 2.0                   October 2012


         server_error
               The authorization server encountered an unexpected
               condition that prevented it from fulfilling the request.
               (This error code is needed because a 500 Internal Server
               Error HTTP status code cannot be returned to the client
               via an HTTP redirect.)

         temporarily_unavailable
               The authorization server is currently unable to handle
               the request due to a temporary overloading or maintenance
               of the server.  (This error code is needed because a 503
               Service Unavailable HTTP status code cannot be returned
               to the client via an HTTP redirect.)

         Values for the "error" parameter MUST NOT include characters
         outside the set %x20-21 / %x23-5B / %x5D-7E.

   error_description
         OPTIONAL.  Human-readable ASCII [USASCII] text providing
         additional information, used to assist the client developer in
         understanding the error that occurred.
         Values for the "error_description" parameter MUST NOT include
         characters outside the set %x20-21 / %x23-5B / %x5D-7E.

   error_uri
         OPTIONAL.  A URI identifying a human-readable web page with
         information about the error, used to provide the client
         developer with additional information about the error.
         Values for the "error_uri" parameter MUST conform to the
         URI-reference syntax and thus MUST NOT include characters
         outside the set %x21 / %x23-5B / %x5D-7E.

   state
         REQUIRED if a "state" parameter was present in the client
         authorization request.  The exact value received from the client