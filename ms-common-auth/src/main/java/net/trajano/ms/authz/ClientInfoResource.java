package net.trajano.ms.authz;

import io.swagger.annotations.Api;
import net.trajano.ms.auth.spi.ClientValidator;
import net.trajano.ms.core.ErrorResponses;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * Client info endpoint resource.
 *
 * @author Archimedes Trajano
 */
@Api
@Path("/client")
@PermitAll
public class ClientInfoResource {

    @Autowired
    private ClientValidator clientValidator;

    @GET
    public Response validateClientInfo(@HeaderParam("Origin") final String origin,
        @HeaderParam("Referer") final String referrer,
        @HeaderParam(HttpHeaders.AUTHORIZATION) final String authorization) {

        if (origin == null && referrer == null) {
            throw ErrorResponses.invalidRequest("Missing Origin");
        }
        if (authorization == null) {
            throw ErrorResponses.invalidRequest("Missing Authorization");
        }
        final URI originUri;
        try {
            final URL tempOrigin;
            if (origin == null && referrer != null) {
                tempOrigin = new URL(referrer);
            } else {
                tempOrigin = new URL(origin);
            }
            originUri = getPartsForOriginHeader(tempOrigin);
        } catch (MalformedURLException e) {
            throw ErrorResponses.invalidRequest("Invalid Origin");
        }

        if (clientValidator.isOriginAllowedFromAuthorization(originUri, authorization)) {
            return Response.ok().build();
        } else {
            throw ErrorResponses.invalidRequest("Invalid Origin for Client");
        }
    }

    /**
     * Strips off the path components from a URL.
     * 
     * @param url
     *            URL to process
     * @return a URI suitable for Access-Control-Allow-Origin
     * @throws MalformedURLException
     *             invalid URL
     */
    public static URI getPartsForOriginHeader(URL url) throws MalformedURLException {

        final String tempOriginString = new URL(url, "/").toString();
        return URI.create(tempOriginString.substring(0, tempOriginString.length() - 1));
    }
}
