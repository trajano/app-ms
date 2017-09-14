package net.trajano.ms.common.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.jar.Manifest;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

import com.google.gson.Gson;

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

    private String json;

    private String rawContent;

    @ApiOperation(value = "Build Info",
        notes = "Reads the MANIFEST.MF and write out the contents.")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getManifest() {

        return rawContent;
    }

    @ApiOperation(value = "Build Info",
        notes = "Reads the MANIFEST.MF and write out the contents.")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getManifestAsJson() {

        return json;
    }

    /**
     * Preloads the manifest so it does not need to be evaluated anymore.
     */
    @PostConstruct
    public void init() throws IOException {

        final Manifest mf = new Manifest(Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/MANIFEST.MF"));
        json = new Gson().toJson(mf.getMainAttributes());
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mf.write(baos);
        rawContent = new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

}
