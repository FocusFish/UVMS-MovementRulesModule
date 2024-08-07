package fish.focus.uvms.movementrules.rest.service.arquillian.tests;

import fish.focus.schema.exchange.module.v1.SendMovementToPluginRequest;
import fish.focus.schema.movementrules.customrule.v1.AvailabilityType;
import fish.focus.schema.movementrules.customrule.v1.SubCriteriaType;
import fish.focus.schema.movementrules.customrule.v1.SubscriptionTypeType;
import fish.focus.schema.movementrules.module.v1.GetTicketsAndRulesByMovementsRequest;
import fish.focus.schema.movementrules.module.v1.GetTicketsAndRulesByMovementsResponse;
import fish.focus.uvms.exchange.model.mapper.JAXBMarshaller;
import fish.focus.uvms.movementrules.model.dto.MovementDetails;
import fish.focus.uvms.movementrules.rest.service.arquillian.BuildRulesRestDeployment;
import fish.focus.uvms.movementrules.rest.service.arquillian.JMSHelper;
import fish.focus.uvms.movementrules.rest.service.arquillian.RulesTestHelper;
import fish.focus.uvms.movementrules.service.bean.RulesServiceBean;
import fish.focus.uvms.movementrules.service.dao.RulesDao;
import fish.focus.uvms.movementrules.service.entity.*;
import fish.focus.uvms.movementrules.service.mapper.CustomRuleMapper;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.jms.TextMessage;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class InternalRestResourceTest extends BuildRulesRestDeployment {

    @Inject
    private RulesServiceBean rulesService;

    @Inject
    private RulesDao rulesDao;

    @Inject
    private JMSHelper jmsHelper;

    @Test
    @OperateOnDeployment("normal")
    public void getTicketsAndRulesByMovementsEventTest() {

        final List<String> movementGuidList = new ArrayList<>();
        final String movementGuid = UUID.randomUUID().toString();
        movementGuidList.add(movementGuid);

        // Create a CustomRule
        CustomRule customRule = CustomRuleMapper.toCustomRuleEntity(RulesTestHelper.getCompleteNewCustomRule());
        customRule.setAvailability(AvailabilityType.GLOBAL);
        customRule.setUpdatedBy("TestUser");
        RuleSubscription ruleSubscription = new RuleSubscription();
        ruleSubscription.setType(SubscriptionTypeType.TICKET);
        ruleSubscription.setCustomRule(customRule);
        ruleSubscription.setOwner("TestUser");
        customRule.getRuleSubscriptionList().add(ruleSubscription);
        customRule = rulesDao.createCustomRule(customRule);

        assertNotNull(customRule);

        // Create a Ticket
        Ticket ticket = RulesTestHelper.getCompleteTicket();
        ticket.setRuleGuid(customRule.getGuid().toString());
        ticket.setUpdatedBy("TestUser");
        ticket.setMovementGuid(movementGuid);
        ticket = rulesDao.createTicket(ticket);

        assertNotNull(ticket);

        GetTicketsAndRulesByMovementsRequest request = new GetTicketsAndRulesByMovementsRequest();
        request.getMovementGuids().addAll(movementGuidList);

        Response response = getWebTarget()
                .path("internal")
                .path("tickets-and-rules-by-movement")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(request));
        assertEquals(Status.OK.getStatusCode(), response.getStatus());

        GetTicketsAndRulesByMovementsResponse retVal = response.readEntity(GetTicketsAndRulesByMovementsResponse.class);
        String retrievedMovementGuid = retVal.getTicketsAndRules().get(0).getTicket().getMovementGuid();
        assertEquals(movementGuid, retrievedMovementGuid);

        rulesDao.removeTicketAfterTests(ticket);
        rulesDao.removeCustomRuleAfterTests(customRule);
    }

    @Test
    @OperateOnDeployment("normal")
    public void evaluateAndTriggerRuleSendToPlugin() throws Exception {
        jmsHelper.clearExchangeQueue();

        String flagState = "SWE";
        MovementDetails movementDetails = getMovementDetails();
        movementDetails.setFlagState(flagState);

        CustomRule customRule = RulesTestHelper.createBasicCustomRule();
        List<RuleSegment> segments = new ArrayList<>();
        RuleSegment segment = new RuleSegment();
        segment.setCriteria("ASSET");
        segment.setSubCriteria("FLAG_STATE");
        segment.setCondition("EQ");
        segment.setValue(flagState);
        segment.setLogicOperator("NONE");
        segment.setCustomRule(customRule);
        segment.setOrder(0);
        segments.add(segment);
        customRule.setRuleSegmentList(segments);
        List<RuleAction> actions = new ArrayList<>();
        RuleAction action = new RuleAction();
        action.setCustomRule(customRule);
        action.setAction("SEND_REPORT");
        action.setTarget("FLUX");
        action.setValue("DNK");
        action.setOrder(1);
        actions.add(action);
        customRule.setRuleActionList(actions);
        rulesService.createCustomRule(customRule, "", "");

        Response response = getWebTarget()
                .path("internal")
                .path("evaluate")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(movementDetails));
        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        TextMessage message = (TextMessage) jmsHelper.getMessageFromExchangeQueue();

        assertThat(message, is(notNullValue()));

        SendMovementToPluginRequest sendMovementRequest = JAXBMarshaller.unmarshallTextMessage(message, SendMovementToPluginRequest.class);
        assertThat(sendMovementRequest, is(notNullValue()));

        assertThat(sendMovementRequest.getReport().getMovement().getConnectId(), is(movementDetails.getConnectId()));

        rulesDao.removeCustomRuleAfterTests(customRule);
    }

    @Test
    @OperateOnDeployment("normal")
    public void evaluateAndTriggerRuleWithLongTermParked() throws Exception {
        jmsHelper.clearExchangeQueue();

        String flagState = "SWE";
        MovementDetails movementDetails = getMovementDetails();
        movementDetails.setParked(true);
        movementDetails.setFlagState(flagState);
        movementDetails.setSource("AIS");

        CustomRule customRule = RulesTestHelper.createBasicCustomRule();
        List<RuleSegment> segments = new ArrayList<>();
        RuleSegment segment = new RuleSegment();
        segment.setCriteria("ASSET");
        segment.setSubCriteria(SubCriteriaType.ASSET_PARKED.value());
        segment.setCondition("EQ");
        segment.setValue("true");
        segment.setLogicOperator("NONE");
        segment.setCustomRule(customRule);
        segment.setOrder(0);
        segments.add(segment);
        customRule.setRuleSegmentList(segments);
        List<RuleAction> actions = new ArrayList<>();
        RuleAction action = new RuleAction();
        action.setCustomRule(customRule);
        action.setAction("SEND_REPORT");
        action.setTarget("FLUX");
        action.setValue("DNK");
        action.setOrder(1);
        actions.add(action);
        customRule.setRuleActionList(actions);
        rulesService.createCustomRule(customRule, "", "");

        Response response = getWebTarget()
                .path("internal")
                .path("evaluate")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(movementDetails));
        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        TextMessage message = (TextMessage) jmsHelper.getMessageFromExchangeQueue();

        assertThat(message, is(notNullValue()));

        SendMovementToPluginRequest sendMovementRequest = JAXBMarshaller.unmarshallTextMessage(message, SendMovementToPluginRequest.class);
        assertThat(sendMovementRequest, is(notNullValue()));

        assertThat(sendMovementRequest.getReport().getMovement().getConnectId(), is(movementDetails.getConnectId()));

        rulesDao.removeCustomRuleAfterTests(customRule);
    }

    private MovementDetails getMovementDetails() {
        MovementDetails movementDetails = new MovementDetails();
        movementDetails.setMovementGuid(UUID.randomUUID().toString());
        movementDetails.setMobileTerminalGuid(UUID.randomUUID().toString());
        movementDetails.setConnectId(UUID.randomUUID().toString());
        movementDetails.setLatitude(11d);
        movementDetails.setLongitude(56d);
        movementDetails.setPositionTime(Instant.now());
        movementDetails.setSource("INMARSAT_C");
        movementDetails.setAssetGuid(UUID.randomUUID().toString());
        movementDetails.setFlagState("SWE");
        return movementDetails;
    }
}
