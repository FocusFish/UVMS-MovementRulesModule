package fish.focus.uvms.movementrules.rest.service;

import fish.focus.schema.movementrules.module.v1.GetTicketsAndRulesByMovementsRequest;
import fish.focus.schema.movementrules.module.v1.GetTicketsAndRulesByMovementsResponse;
import fish.focus.uvms.movementrules.model.dto.MovementDetails;
import fish.focus.uvms.movementrules.service.bean.CustomRulesEvaluator;
import fish.focus.uvms.movementrules.service.bean.RulesServiceBean;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/internal")
@Stateless
@Consumes(value = {MediaType.APPLICATION_JSON})
@Produces(value = {MediaType.APPLICATION_JSON})
public class InternalRestResources {

    @Inject
    private RulesServiceBean rulesService;

    @Inject
    private CustomRulesEvaluator customRuleEvaluator;

    @POST
    @Path("/tickets-and-rules-by-movement")
    public Response getTicketsAndRulesByMovementsEvent(GetTicketsAndRulesByMovementsRequest request) {
        GetTicketsAndRulesByMovementsResponse response =
                rulesService.getTicketsAndRulesByMovements(request.getMovementGuids());
        return Response.ok(response).build();
    }

    @POST
    @Path("/evaluate")
    public Response evaluateCustomRules(MovementDetails movementDetails) {
        try {
            customRuleEvaluator.evaluate(movementDetails);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().entity(ExceptionUtils.getRootCause(e)).build();
        }
    }
}
