package fish.focus.uvms.movementrules.service;

import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import fish.focus.uvms.asset.client.model.SimpleCreatePoll;

@Path("asset/rest/internal/")
@Stateless
@Consumes(value = {MediaType.APPLICATION_JSON})
@Produces(value = {MediaType.APPLICATION_JSON})
public class AssetModuleMock {

    @POST
    @Path("/createPollForAsset/{id}")
    public Response createPoll(@PathParam("id") String assetId, @QueryParam("username") String username, SimpleCreatePoll createPoll) {
        System.setProperty("AssetPollEndpointReached", "True");
        return Response.ok().entity(Boolean.TRUE).build();
    }

}
