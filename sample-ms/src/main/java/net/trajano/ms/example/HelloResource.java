package net.trajano.ms.example;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.security.PermitAll;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import net.trajano.ms.common.oauth.OAuthTokenResponse;
import net.trajano.ms.example.domain.MyType;

@SwaggerDefinition(
    info = @Info(
        title = "Sample microservice from hello",
        version = "1.0"))
@Api(tags = {
    "infernal",
    "doom"
})
@Component
@Path("/hello")
@PermitAll
public class HelloResource {

    @Context
    private Client jaxrsClient;

    @ApiOperation(value = "displays openid config of google async",
        hidden = true)
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/async")
    public void async(@Suspended final AsyncResponse asyncResponse) throws Exception {

        final Future<Response> futureResponseFromClient = jaxrsClient.target("https://accounts.google.com/.well-known/openid-configuration").request().header(javax.ws.rs.core.HttpHeaders.USER_AGENT, "curl/7.55.1").async().get();

        final Response responseFromClient = futureResponseFromClient.get();
        try {
            final String object = responseFromClient.readEntity(String.class);
            asyncResponse.resume(object);
        } finally {
            responseFromClient.close();
        }
    }

    @ApiOperation(value = "throws an client exception")
    @GET
    @Path("/bad")
    public Response badClient() {

        final OAuthTokenResponse entity = new OAuthTokenResponse();
        entity.setError("client bad");
        throw new BadRequestException("who's bad", Response.status(Status.BAD_REQUEST).entity(entity).build());
    }

    @ApiOperation(value = "throws a Runtime Exception")
    @GET
    @Path("/cough")
    public Response cough() {

        throw new RuntimeException("ahem", new IOException("burp"));
    }

    private String getFileName(final MultivaluedMap<String, String> header) {

        final String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

        for (final String filename : contentDisposition) {
            if (filename.trim().startsWith("filename")) {

                final String[] name = filename.split("=");

                final String finalFileName = name[1].trim().replaceAll("\"", "");
                return finalFileName;
            }
        }
        return "unknown";
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public String helloHelloPost(@FormParam("me") final String me) {

        return "HelloHello " + me;
    }

    @ApiOperation(value = "displays hello world")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String helloWorld() {

        return "Hello world at " + new Date();
    }

    @ApiOperation(value = "displays hello world")
    @GET
    @Path("/o")
    @Produces({
        MediaType.APPLICATION_JSON,
        MediaType.APPLICATION_XML
    })
    public MyType helloWorld2() {

        final MyType myType = new MyType();
        myType.setFoo("Hello world at " + new Date());
        return myType;
    }

    @ApiOperation(value = "data from GSON")
    @GET
    @Path("/j")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject jsonObject() {

        final JsonObject ret = new JsonObject();
        ret.add("Hello", new JsonPrimitive("world"));
        return ret;
    }

    @ApiOperation(value = "displays openid config of google")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/openid")
    public Response maven() {

        final Response clientResponse = jaxrsClient.target("https://accounts.google.com/.well-known/openid-configuration").request().header(javax.ws.rs.core.HttpHeaders.USER_AGENT, "curl/7.55.1").get();
        try {
            final String value = clientResponse.readEntity(String.class);
            return Response.ok(value).build();
        } finally {
            clientResponse.close();
        }
    }

    @ApiOperation(value = "Cancelling after 1 seconds",
        tags = "internal")
    @GET
    @Path("/s1")
    @Produces(MediaType.TEXT_PLAIN)
    public void s1(@Suspended final AsyncResponse asyncResponse) throws InterruptedException {

        asyncResponse.setTimeout(5, TimeUnit.SECONDS);
        Thread.sleep(1000);
        asyncResponse.cancel(5);
    }

    @ApiOperation(value = "Timeout after 2 seconds")
    @GET
    @Path("/st")
    @Produces(MediaType.TEXT_PLAIN)
    public void st(@Suspended final AsyncResponse asyncResponse) throws InterruptedException {

        asyncResponse.setTimeout(1, TimeUnit.SECONDS);
        Thread.sleep(2000);
        asyncResponse.resume(Response.ok("hello").build());
    }

    @ApiOperation(value = "displays hello world after 5 seconds")
    @GET
    @Path("/suspend")
    @Produces(MediaType.TEXT_PLAIN)
    public void suspend(@Suspended final AsyncResponse asyncResponse) throws InterruptedException {

        Thread.sleep(2000);
        asyncResponse.resume(Response.ok("hello").build());
    }

    @ApiOperation(value = "upload a file")
    @POST
    @Path("/upload")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public void upload(
        final MultipartFormDataInput input,
        @Suspended final AsyncResponse asyncResponse) throws IOException {

        final JsonObject json = new JsonObject();
        final Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        final List<InputPart> inputParts = uploadForm.get("uploadedFile");

        for (final InputPart inputPart : inputParts) {

            final MultivaluedMap<String, String> header = inputPart.getHeaders();
            final String fileName = getFileName(header);

            //convert the uploaded file to inputstream
            final InputStream inputStream = inputPart.getBody(InputStream.class, null);
            int c = 0;
            while (inputStream.read() != -1) {
                ++c;
            }

            json.add(fileName, new JsonPrimitive(c));
        }
        asyncResponse.resume(json);
    }
}
