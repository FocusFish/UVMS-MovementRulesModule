package fish.focus.uvms.movementrules.rest.service.arquillian.tests;

import fish.focus.schema.movementrules.customrule.v1.AssetStatus;
import fish.focus.schema.movementrules.customrule.v1.AvailabilityType;
import fish.focus.schema.movementrules.customrule.v1.LogicOperatorType;
import fish.focus.schema.movementrules.customrule.v1.MobileTerminalStatus;
import fish.focus.schema.movementrules.ticket.v1.TicketStatusType;
import fish.focus.uvms.movementrules.rest.service.arquillian.BuildRulesRestDeployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(Arquillian.class)
public class ConfigResourcesTest extends BuildRulesRestDeployment {

    @Test
    @OperateOnDeployment("normal")
    public void getConfigTest() {
        Response response = getWebTarget()
                .path("config/")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get();
        assertEquals(Status.OK.getStatusCode(), response.getStatus());

        Map configMap = response.readEntity(Map.class);

        Map<String, HashMap<String, ArrayList<String>>> criteriaMap
                = (Map<String, HashMap<String, ArrayList<String>>>) configMap.get("CRITERIA");
        assertFalse(criteriaMap.isEmpty());

        Map actionMap = (Map) configMap.get("ACTIONS");
        assertFalse(actionMap.isEmpty());

        List<LogicOperatorType> logicOperators = (List<LogicOperatorType>) configMap.get("LOGIC_OPERATORS");
        assertFalse(logicOperators.isEmpty());

        List<AvailabilityType> availabilities = (List<AvailabilityType>) configMap.get("AVAILABILITY");
        assertFalse(availabilities.isEmpty());

        List<MobileTerminalStatus> terminalStatuses = (List<MobileTerminalStatus>) configMap.get("MOBILETERMINAL_STATUSES");
        assertFalse(terminalStatuses.isEmpty());

        List<AssetStatus> assetStatuses = (List<AssetStatus>) configMap.get("ASSET_STATUSES");
        assertFalse(assetStatuses.isEmpty());
    }


    @Test
    @OperateOnDeployment("normal")
    public void getTicketStatusesTest() {
        List<TicketStatusType> response = getWebTarget()
                .path("config/ticketstatus")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getToken())
                .get(new GenericType<List<TicketStatusType>>(){});

        assertEquals(3, response.size());
    }
}
