Sample Microservice
===================

The approach is package-by-feature, but each microservice in itself is tends to provide a single feature so we end up looking as package-by-layer.  Primarily we'd reduce the amount of `import` statements to manage.

Still the approach provided tries to limit the number of packages and making them anemic.

The main root package contains the JAX-RS classes that are used by the microservice along with the actual executable (which in itself is a JAX-RS Application).

The `@Api` `@Path` classes are suffixed with `Resource` to help visually see what their purpose is.

The `Applicaton` class is suffixed with `MS`.

Spring bean implementations go into `beans` subpackage.  This separation helps distinguish the component classes from the exposed resources.

The `@XmlRootElement` classes aka as  domain objects are placed in `domain` subpackage.

The separation is intended to make it easier to refactor common groups primarily the domain subpackage into a library of application level domain classes if it appears to be growing too much. 

