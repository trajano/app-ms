Microservices Base
==================

This provides a parent project POM used to quickly bootstrap microservice development.

Primarily it provides the [Microservice Common](https://site.trajano.net/app-ms/ms-common/index.html) project which provides almost everything that is needed to get the microservices running along with the dependencies.

This project also provides the build number information that gets populated into META-INF/MANIFEST.  The data being accessible using the `/.well-known/manifest` endpoint provided by every microservice.

Finally, it provides a check to make sure the `<build>/<finalName>` is set so that when building `Dockerfile`s it is easier as it does not have to be updated when the version changes.