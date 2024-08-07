package fish.focus.uvms.movementrules.service.message.bean;

import fish.focus.schema.movementrules.customrule.v1.AvailabilityType;
import fish.focus.schema.movementrules.ticket.v1.TicketStatusType;
import fish.focus.schema.movementrules.ticket.v1.TicketType;
import fish.focus.uvms.commons.date.JsonBConfigurator;
import fish.focus.uvms.commons.message.api.MessageConstants;
import fish.focus.uvms.movementrules.model.dto.MovementDetails;
import fish.focus.uvms.movementrules.service.BuildRulesServiceDeployment;
import fish.focus.uvms.movementrules.service.RulesTestHelper;
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

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.*;
import javax.json.bind.Jsonb;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class EventStreamSenderTest extends BuildRulesServiceDeployment {

    private static final String user = "user";
    private static Jsonb jsonb;

    static {
        JsonBConfigurator configurator = new JsonBConfigurator();
        jsonb = configurator.getContext(null);
    }

    private MessageConsumer subscriber;

    @Inject
    private RulesServiceBean rulesService;

    @Inject
    private ValidationServiceBean validationService;

    @Inject
    private RulesDao rulesDao;

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Test
    @OperateOnDeployment("normal")
    public void eventStreamSendTicketTest() throws Exception {
        CustomRule customRule = createCustomRule(user);
        CustomRule createdCustomRule = rulesService.createCustomRule(customRule, "", "");
        MovementDetails movementDetails = getMovementDetails();
        registerSubscriber();
        validationService.customRuleTriggered(createdCustomRule.getName(), createdCustomRule.getGuid().toString(), movementDetails, "CREATE_TICKET");

        TextMessage message = (TextMessage) listenOnEventStream(10000L);
        assertNotNull(message);
        assertEquals("Ticket", message.getStringProperty(MessageConstants.EVENT_STREAM_EVENT));
        assertTrue(message.getStringProperty(MessageConstants.EVENT_STREAM_SUBSCRIBER_LIST).contains(user));

        TicketType ticket = jsonb.fromJson(message.getText(), TicketType.class);
        assertThat(ticket.getRuleName(), is(customRule.getName()));
        assertThat(ticket.getMovementGuid(), is(movementDetails.getMovementGuid()));
        assertThat(ticket.getAssetGuid(), is(movementDetails.getAssetGuid()));

        rulesDao.removeCustomRuleAfterTests(customRule);
    }

    @Test
    @OperateOnDeployment("normal")
    public void eventStreamRuleWithGlobalAvailabilityTest() throws Exception {
        String flagstate = "SWE";

        CustomRule customRule = createCustomRule(null);

        CustomRule createdCustomRule = rulesService.createCustomRule(customRule, "", "");
        createdCustomRule.setAvailability(AvailabilityType.GLOBAL);
        CustomRule updatedRule = rulesService.updateCustomRule(createdCustomRule);

        MovementDetails movementDetails = getMovementDetails();
        movementDetails.setFlagState(flagstate);

        registerSubscriber();
        validationService.customRuleTriggered(updatedRule.getName(), updatedRule.getGuid().toString(), movementDetails, "CREATE_TICKET");

        TextMessage message = (TextMessage) listenOnEventStream(10000L);
        assertNotNull(message);
        assertEquals("Ticket", message.getStringProperty(MessageConstants.EVENT_STREAM_EVENT));
        assertNull(message.getStringProperty(MessageConstants.EVENT_STREAM_SUBSCRIBER_LIST));

        TicketType ticket = jsonb.fromJson(message.getText(), TicketType.class);
        assertThat(ticket.getRuleName(), is(updatedRule.getName()));
        assertThat(ticket.getMovementGuid(), is(movementDetails.getMovementGuid()));
        assertThat(ticket.getAssetGuid(), is(movementDetails.getAssetGuid()));
        rulesDao.removeTicketAfterTests(TicketMapper.toTicketEntity(ticket));
        rulesDao.removeCustomRuleAfterTests(customRule);
        rulesDao.removeCustomRuleAfterTests(updatedRule);
    }

    @Test
    @OperateOnDeployment("normal")
    public void eventStreamUpdatedTicketTest() throws Exception {
        String flagstate = "SWE";

        CustomRule customRule = createCustomRule(user);

        CustomRule createdCustomRule = rulesService.createCustomRule(customRule, "", "");

        MovementDetails movementDetails = getMovementDetails();
        movementDetails.setFlagState(flagstate);
        registerSubscriber();
        validationService.customRuleTriggered(createdCustomRule.getName(), createdCustomRule.getGuid().toString(), movementDetails, "CREATE_TICKET");

        TextMessage message = (TextMessage) listenOnEventStream(10000L);
        assertNotNull(message);
        TicketType ticket = jsonb.fromJson(message.getText(), TicketType.class);
        assertThat(ticket.getRuleName(), is(customRule.getName()));

        registerSubscriber();
        ticket.setStatus(TicketStatusType.CLOSED);
        rulesService.updateTicketStatus(TicketMapper.toTicketEntity(ticket));
        message = (TextMessage) listenOnEventStream(10000L);
        assertEquals("TicketUpdate", message.getStringProperty(MessageConstants.EVENT_STREAM_EVENT));

        TicketType ticketUpdate = jsonb.fromJson(message.getText(), TicketType.class);
        assertThat(ticketUpdate.getGuid(), is(ticket.getGuid()));
        assertThat(ticketUpdate.getStatus(), is(TicketStatusType.CLOSED));
        assertThat(ticketUpdate.getMovementGuid(), is(movementDetails.getMovementGuid()));
        assertThat(ticketUpdate.getAssetGuid(), is(movementDetails.getAssetGuid()));

        rulesDao.removeCustomRuleAfterTests(customRule);
    }

    public void registerSubscriber() throws Exception {
        Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic eventBus = session.createTopic("EventStream");
        subscriber = session.createConsumer(eventBus, null, true);
    }

    public Message listenOnEventStream(Long timeoutInMillis) throws Exception {
        try {
            return subscriber.receive(timeoutInMillis);
        } finally {
            subscriber.close();
        }
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
