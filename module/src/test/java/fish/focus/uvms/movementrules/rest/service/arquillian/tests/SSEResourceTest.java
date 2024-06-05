package fish.focus.uvms.movementrules.rest.service.arquillian.tests;

import fish.focus.schema.movementrules.customrule.v1.AvailabilityType;
import fish.focus.schema.movementrules.ticket.v1.TicketStatusType;
import fish.focus.schema.movementrules.ticket.v1.TicketType;
import fish.focus.uvms.movementrules.model.dto.MovementDetails;
import fish.focus.uvms.movementrules.rest.service.arquillian.BuildRulesRestDeployment;
import fish.focus.uvms.movementrules.rest.service.arquillian.RulesTestHelper;
import fish.focus.uvms.movementrules.rest.service.arquillian.SSETestClient;
import fish.focus.uvms.movementrules.service.bean.RulesServiceBean;
import fish.focus.uvms.movementrules.service.bean.ValidationServiceBean;
import fish.focus.uvms.movementrules.service.dao.RulesDao;
import fish.focus.uvms.movementrules.service.entity.CustomRule;
import fish.focus.uvms.movementrules.service.entity.RuleSegment;
import fish.focus.uvms.movementrules.service.mapper.TicketMapper;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Arquillian.class)
public class SSEResourceTest extends BuildRulesRestDeployment {

    private static final String FLAG_STATE_SWE = "SWE";
    private static final String USER = "user";

    @Inject
    private RulesServiceBean rulesService;

    @Inject
    private ValidationServiceBean validationService;

    @Inject
    private RulesDao rulesDao;

    @Test
    @OperateOnDeployment("normal")
    public void sseBroadcastSubscribingToRuleTest() throws Exception {
        CustomRule customRule = createCustomRule(USER);

        CustomRule createdCustomRule = rulesService.createCustomRule(customRule, "", "");

        MovementDetails movementDetails = getMovementDetails();

        try (SSETestClient client = new SSETestClient()) {
            validationService.customRuleTriggered(createdCustomRule.getName(), createdCustomRule.getGuid().toString(), movementDetails, "CREATE_TICKET");

            var ticket = await().atMost(10, SECONDS).until(client::getTicket, is(notNullValue()));
            assertThat(ticket.getRuleName(), is(customRule.getName()));
            assertThat(ticket.getMovementGuid(), is(movementDetails.getMovementGuid()));
            assertThat(ticket.getAssetGuid(), is(movementDetails.getAssetGuid()));
        }
        rulesDao.removeCustomRuleAfterTests(customRule);
    }

    @Test
    @OperateOnDeployment("normal")
    public void sseBroadcastSubscribingToRuleTwoConnectionsTest() throws Exception {
        CustomRule customRule = createCustomRule(USER);

        CustomRule createdCustomRule = rulesService.createCustomRule(customRule, "", "");

        MovementDetails movementDetails = getMovementDetails();

        try (SSETestClient client = new SSETestClient();
             SSETestClient client2 = new SSETestClient()) {
            validationService.customRuleTriggered(createdCustomRule.getName(), createdCustomRule.getGuid().toString(), movementDetails, "CREATE_TICKET");

            var ticket = await().atMost(10, SECONDS).until(client::getTicket, is(notNullValue()));
            assertThat(ticket.getRuleName(), is(customRule.getName()));
            assertThat(ticket.getMovementGuid(), is(movementDetails.getMovementGuid()));
            assertThat(ticket.getAssetGuid(), is(movementDetails.getAssetGuid()));

            var ticket2 = await().atMost(10, SECONDS).until(client2::getTicket, is(notNullValue()));
            assertThat(ticket2.getRuleName(), is(customRule.getName()));
            assertThat(ticket2.getMovementGuid(), is(movementDetails.getMovementGuid()));
            assertThat(ticket2.getAssetGuid(), is(movementDetails.getAssetGuid()));
        }
        rulesDao.removeCustomRuleAfterTests(customRule);
    }

    @Test
    @OperateOnDeployment("normal")
    public void sseBroadcastNotSubscribingToRuleTest() throws Exception {
        CustomRule customRule = createCustomRule(null);

        CustomRule createdCustomRule = rulesService.createCustomRule(customRule, "", "");

        MovementDetails movementDetails = getMovementDetails();
        movementDetails.setFlagState(FLAG_STATE_SWE);

        try (SSETestClient client = new SSETestClient()) {
            validationService.customRuleTriggered(createdCustomRule.getName(), createdCustomRule.getGuid().toString(), movementDetails, ";");

            await().pollDelay(4, SECONDS).until(client::getTicket, is(nullValue()));
        }
        rulesDao.removeCustomRuleAfterTests(customRule);
    }

    @Test
    @OperateOnDeployment("normal")
    public void sseBroadcastNotSubscribingToGlobalRuleTest() throws Exception {
        CustomRule customRule = createCustomRule(null);

        CustomRule createdCustomRule = rulesService.createCustomRule(customRule, "", "");
        createdCustomRule.setAvailability(AvailabilityType.GLOBAL);
        CustomRule updatedRule = rulesService.updateCustomRule(createdCustomRule);

        MovementDetails movementDetails = getMovementDetails();
        movementDetails.setFlagState(FLAG_STATE_SWE);

        TicketType ticket;
        try (SSETestClient client = new SSETestClient()) {
            validationService.customRuleTriggered(updatedRule.getName(), updatedRule.getGuid().toString(), movementDetails, "CREATE_TICKET");

            ticket = await().atMost(10, SECONDS).until(client::getTicket, is(notNullValue()));
            assertThat(ticket.getRuleName(), is(updatedRule.getName()));
            assertThat(ticket.getMovementGuid(), is(movementDetails.getMovementGuid()));
            assertThat(ticket.getAssetGuid(), is(movementDetails.getAssetGuid()));
        }
        rulesDao.removeTicketAfterTests(TicketMapper.toTicketEntity(ticket));
        rulesDao.removeCustomRuleAfterTests(customRule);
    }

    @Test
    @OperateOnDeployment("normal")
    public void sseBroadcastTicketUpdateTest() throws Exception {
        CustomRule customRule = createCustomRule(USER);

        CustomRule createdCustomRule = rulesService.createCustomRule(customRule, "", "");

        MovementDetails movementDetails = getMovementDetails();
        movementDetails.setFlagState(FLAG_STATE_SWE);

        try (SSETestClient client = new SSETestClient()) {
            validationService.customRuleTriggered(createdCustomRule.getName(), createdCustomRule.getGuid().toString(), movementDetails, "CREATE_TICKET");

            var ticket = await().atMost(10, SECONDS).until(client::getTicketAndReset, is(notNullValue()));
            assertThat(ticket.getRuleName(), is(customRule.getName()));

            ticket.setStatus(TicketStatusType.CLOSED);
            rulesService.updateTicketStatus(TicketMapper.toTicketEntity(ticket));

            var ticketUpdate = await().atMost(10, SECONDS).until(client::getTicket, is(notNullValue()));
            assertThat(ticketUpdate.getGuid(), is(ticket.getGuid()));
            assertThat(ticketUpdate.getStatus(), is(TicketStatusType.CLOSED));
            assertThat(ticketUpdate.getMovementGuid(), is(movementDetails.getMovementGuid()));
            assertThat(ticketUpdate.getAssetGuid(), is(movementDetails.getAssetGuid()));
        }
        rulesDao.removeCustomRuleAfterTests(customRule);
    }

    private CustomRule createCustomRule(String username) {
        CustomRule customRule = RulesTestHelper.createBasicCustomRule();
        if (username != null) {
            customRule.setUpdatedBy(username);
        }
        RuleSegment segment = new RuleSegment();
        segment.setCriteria("ASSET");
        segment.setSubCriteria("FLAG_STATE");
        segment.setCondition("EQ");
        segment.setValue("SWE");
        segment.setLogicOperator("NONE");
        segment.setCustomRule(customRule);
        segment.setOrder(0);
        customRule.getRuleSegmentList().add(segment);
        return customRule;
    }

    private MovementDetails getMovementDetails() {
        MovementDetails movementDetails = new MovementDetails();
        movementDetails.setMovementGuid(UUID.randomUUID().toString());
        movementDetails.setConnectId(UUID.randomUUID().toString());
        movementDetails.setLatitude(11d);
        movementDetails.setLongitude(56d);
        movementDetails.setPositionTime(Instant.now());
        movementDetails.setSource("INMARSAT_C");
        movementDetails.setAssetGuid(UUID.randomUUID().toString());
        movementDetails.setFlagState("SWE");
        movementDetails.setAreaTypes(new ArrayList<>());
        return movementDetails;
    }
}
