package fish.focus.uvms.movementrules.service.bean;

import fish.focus.schema.movementrules.customrule.v1.ActionType;
import fish.focus.uvms.commons.date.DateUtils;
import fish.focus.uvms.movementrules.model.dto.MovementDetails;
import fish.focus.uvms.movementrules.service.RulesTestHelper;
import fish.focus.uvms.movementrules.service.TransactionalTests;
import fish.focus.uvms.movementrules.service.bean.RulesServiceBean;
import fish.focus.uvms.movementrules.service.bean.ValidationServiceBean;
import fish.focus.uvms.movementrules.service.entity.CustomRule;
import fish.focus.uvms.movementrules.service.entity.RuleAction;
import fish.focus.uvms.movementrules.service.entity.Ticket;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class ValidationServiceBeanTest extends TransactionalTests {

    @Inject
    ValidationServiceBean validationService;

    @Inject
    RulesServiceBean rulesService;

    @Test
    @OperateOnDeployment("normal")
    public void customRuleTriggeredLastTriggeredDateShouldBeSetTest() throws Exception {

        Instant timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        CustomRule customRule = RulesTestHelper.createCompleteCustomRule();
        CustomRule createdCustomRule = rulesService.createCustomRule(customRule, "", "");

        MovementDetails movementFact = RulesTestHelper.createBasicMovementDetails();
        validationService.customRuleTriggered(createdCustomRule.getName(), createdCustomRule.getGuid().toString(), movementFact, "EMAIL,test@test.com");

        CustomRule updatedCustomRule = rulesService.getCustomRuleByGuid(createdCustomRule.getGuid());
        String lastTriggered = DateUtils.dateToEpochMilliseconds(updatedCustomRule.getLastTriggered());
        assertThat(lastTriggered, is(notNullValue()));

        Instant dateTriggered = DateUtils.stringToDate(lastTriggered);
        assertTrue(dateTriggered.toEpochMilli() >= timestamp.toEpochMilli());
    }

    @Test
    @OperateOnDeployment("normal")
    public void customRuleTriggeredNewTicketShouldBeCreatedTest() throws Exception {
        CustomRule customRule = RulesTestHelper.createCompleteCustomRule();
        CustomRule createdCustomRule = rulesService.createCustomRule(customRule, "", "");

        long openTicketsBefore = validationService.getNumberOfOpenTickets(createdCustomRule.getUpdatedBy());

        MovementDetails movementFact = RulesTestHelper.createBasicMovementDetails();
        validationService.customRuleTriggered(createdCustomRule.getName(), createdCustomRule.getGuid().toString(), movementFact, "CREATE_TICKET,null");

        long openTicketsAfter = validationService.getNumberOfOpenTickets(createdCustomRule.getUpdatedBy());
        assertThat(openTicketsAfter, is(openTicketsBefore + 1));
    }

    @Test
    @OperateOnDeployment("normal")
    public void customRuleTriggeredSendToNAFTest() throws Exception {
        CustomRule customRule = RulesTestHelper.createCompleteCustomRule();
        CustomRule createdCustomRule = rulesService.createCustomRule(customRule, "", "");

        long openTicketsBefore = validationService.getNumberOfOpenTickets(createdCustomRule.getUpdatedBy());

        MovementDetails movementFact = RulesTestHelper.createBasicMovementDetails();
        validationService.customRuleTriggered(createdCustomRule.getName(), createdCustomRule.getGuid().toString(), movementFact, "SEND_REPORT,NAF,SWE;CREATE_TICKET,null");

        long openTicketsAfter = validationService.getNumberOfOpenTickets(createdCustomRule.getUpdatedBy());
        assertThat(openTicketsAfter, is(openTicketsBefore + 1));
    }

    @Test
    @OperateOnDeployment("normal")
    public void customRuleTriggeredCreatePollTest() throws Exception {

        System.setProperty("AssetPollEndpointReached", "False");
        Instant timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        CustomRule customRule = RulesTestHelper.createCompleteCustomRule();

        RuleAction action = new RuleAction();
        action.setAction(ActionType.MANUAL_POLL.value());
        action.setValue("Dont Care About This");
        action.setOrder(5);
        action.setCustomRule(customRule);
        customRule.getRuleActionList().add(action);

        CustomRule createdCustomRule = rulesService.createCustomRule(customRule, "", "");

        MovementDetails movementFact = RulesTestHelper.createBasicMovementDetails();
        validationService.customRuleTriggered(createdCustomRule.getName(), createdCustomRule.getGuid().toString(), movementFact, "MANUAL_POLL,ThisDoesNotMatter");

        assertEquals("True", System.getProperty("AssetPollEndpointReached"));
        System.clearProperty("AssetPollEndpointReached");
    }

    @Test
    @OperateOnDeployment("normal")
    public void customRuleTriggeredSendEmailTest() throws Exception {

        System.setProperty("ExchangeEmailEndpointReached", "False");
        Instant timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        CustomRule customRule = RulesTestHelper.createCompleteCustomRule();

        RuleAction action = new RuleAction();
        action.setAction(ActionType.EMAIL.value());
        action.setValue("Dont Care About This");
        action.setOrder(5);
        action.setCustomRule(customRule);
        customRule.getRuleActionList().add(action);

        CustomRule createdCustomRule = rulesService.createCustomRule(customRule, "", "");

        MovementDetails movementFact = RulesTestHelper.createBasicMovementDetails();
        validationService.customRuleTriggered(createdCustomRule.getName(), createdCustomRule.getGuid().toString(), movementFact, "EMAIL,ThisDoesNotMatter");

        assertEquals("True", System.getProperty("ExchangeEmailEndpointReached"));
        System.clearProperty("ExchangeEmailEndpointReached");
    }


    @Test
    @OperateOnDeployment("normal")
    public void aggregateRuleTriggeredNewTicketShouldBeCreatedTest() throws Exception {
        CustomRule customRule = RulesTestHelper.createCompleteCustomRule();
        customRule.setAggregateInvocations(true);
        CustomRule createdCustomRule = rulesService.createCustomRule(customRule, "", "");

        long openTicketsBefore = validationService.getNumberOfOpenTickets(createdCustomRule.getUpdatedBy());

        MovementDetails movementFact = RulesTestHelper.createBasicMovementDetails();
        validationService.customRuleTriggered(createdCustomRule.getName(), createdCustomRule.getGuid().toString(), movementFact, "CREATE_TICKET,null");

        long openTicketsAfter = validationService.getNumberOfOpenTickets(createdCustomRule.getUpdatedBy());
        assertThat(openTicketsAfter, is(openTicketsBefore + 1));
    }
    
    @Test
    @OperateOnDeployment("normal")
    public void aggregateRuleTriggeredTicketCountShouldIncreaseTest() throws Exception {
        CustomRule customRule = RulesTestHelper.createCompleteCustomRule();
        customRule.setAggregateInvocations(true);
        CustomRule createdCustomRule = rulesService.createCustomRule(customRule, "", "");

        MovementDetails movementFact = RulesTestHelper.createBasicMovementDetails();
        validationService.customRuleTriggered(createdCustomRule.getName(), createdCustomRule.getGuid().toString(), movementFact, "CREATE_TICKET,null");

        MovementDetails movementFact2 = RulesTestHelper.createBasicMovementDetails();
        validationService.customRuleTriggered(createdCustomRule.getName(), createdCustomRule.getGuid().toString(), movementFact2, "CREATE_TICKET,null");
        
        List<Ticket> tickets = rulesService.getTicketsByMovements(Arrays.asList(movementFact.getMovementGuid()));
        assertThat(tickets.size(), is(1));
        assertThat(tickets.get(0).getTicketCount(), is(2l));
    }
    
    @Test
    @OperateOnDeployment("normal")
    public void aggregateRuleTriggeredDateTriggeredShouldUpdateTest() throws Exception {
        CustomRule customRule = RulesTestHelper.createCompleteCustomRule();
        customRule.setAggregateInvocations(true);
        CustomRule createdCustomRule = rulesService.createCustomRule(customRule, "", "");

        MovementDetails movementFact = RulesTestHelper.createBasicMovementDetails();
        validationService.customRuleTriggered(createdCustomRule.getName(), createdCustomRule.getGuid().toString(), movementFact, "CREATE_TICKET,null");

        Instant firstTimestamp = rulesService.getCustomRuleByGuid(createdCustomRule.getGuid())
                .getLastTriggered();
        
        MovementDetails movementFact2 = RulesTestHelper.createBasicMovementDetails();
        validationService.customRuleTriggered(createdCustomRule.getName(), createdCustomRule.getGuid().toString(), movementFact2, "EMAIL,test@test.com");

        Instant secondTimestamp = rulesService.getCustomRuleByGuid(createdCustomRule.getGuid())
                .getLastTriggered();

        assertTrue(firstTimestamp.isBefore(secondTimestamp));
    }
}
