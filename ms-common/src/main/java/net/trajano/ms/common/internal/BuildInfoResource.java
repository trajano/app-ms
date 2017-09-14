package net.trajano.ms.common.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.jar.Manifest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * This endpoint is exposed by every microservice to provide JWKS that is used
 * by the microservice.
 *
 * @author Archimedes Trajano
 */
@Component
@Api
@Path("/build-info")
public class BuildInfoResource {

    @ApiOperation(value = "Build Info",
        notes = "Reads the MANIFEST.MF and write out the contents.")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public InputStream getManifest() throws IOException {

        return Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/MANIFEST.MF");
    }

    @ApiOperation(value = "Build Info",
        notes = "Reads the MANIFEST.MF and write out the contents.")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<Object, Object> getManifestAttributes() throws IOException {

        final Manifest mf = new Manifest(Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/MANIFEST.MF"));
        return mf.getMainAttributes();
    }

}
