package fish.focus.uvms.movementrules.service.bean;

import fish.focus.uvms.movementrules.model.dto.MovementDetails;
import fish.focus.uvms.movementrules.service.RulesTestHelper;
import fish.focus.uvms.movementrules.service.TransactionalTests;
import fish.focus.uvms.movementrules.service.business.RulesValidator;
import fish.focus.uvms.movementrules.service.entity.CustomRule;
import fish.focus.uvms.movementrules.service.entity.PreviousReport;
import fish.focus.uvms.movementrules.service.entity.RuleSegment;
import fish.focus.uvms.movementrules.service.entity.Ticket;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.transaction.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Arquillian.class)
public class CustomRulesEvaluatorTest extends TransactionalTests {

    @Inject
    private CustomRulesEvaluator customRulesEvaluator;

    @Inject
    private RulesServiceBean rulesService;

    @Inject
    private RulesValidator rulesValidator;

    @Before
    public void reloadCustomRules() throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
        rulesService.getRunnableCustomRules().stream().forEach(rule -> rule.setActive(false));
        rulesValidator.updateCustomRules(); // reload/clear rules
        userTransaction.commit();
        userTransaction.begin();
    }

    @Test
    @OperateOnDeployment("normal")
    public void evaluateMovementAndVerifyReportCreated() {
        List<PreviousReport> previousReportsBefore = rulesService.getPreviousMovementReports();

        MovementDetails movementDetails = getMovementDetails();
        customRulesEvaluator.evaluate(movementDetails);

        List<PreviousReport> previousReportsAfter = rulesService.getPreviousMovementReports();
        assertThat(previousReportsAfter.size(), is(previousReportsBefore.size() + 1));
    }


    @Test
    @OperateOnDeployment("normal")
    public void evaluateMovementTriggerFlagStateRule() throws Exception {
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
        rulesService.createCustomRule(customRule, "", "");

        customRulesEvaluator.evaluate(movementDetails);

        List<Ticket> tickets = rulesService.getTicketsByMovements(Arrays.asList(movementDetails.getMovementGuid()));
        assertThat(tickets.size(), is(1));
    }

    @Test
    @OperateOnDeployment("normal")
    public void evaluateMovementTriggerAreaRule() throws Exception {
        MovementDetails movementDetails = getMovementDetails();
        // AreaA
        movementDetails.setLongitude(1d);
        movementDetails.setLatitude(1d);

        CustomRule customRule = RulesTestHelper.createBasicCustomRule();
        List<RuleSegment> segments = new ArrayList<>();
        RuleSegment segment = new RuleSegment();
        segment.setCriteria("AREA");
        segment.setSubCriteria("AREA_CODE");
        segment.setCondition("EQ");
        segment.setValue("AreaA");
        segment.setLogicOperator("NONE");
        segment.setCustomRule(customRule);
        segment.setOrder(0);
        segments.add(segment);
        customRule.setRuleSegmentList(segments);
        rulesService.createCustomRule(customRule, "", "");

        customRulesEvaluator.evaluate(movementDetails);

        List<Ticket> tickets = rulesService.getTicketsByMovements(Arrays.asList(movementDetails.getMovementGuid()));
        assertThat(tickets.size(), is(1));
    }

    @Test
    @OperateOnDeployment("normal")
    public void evaluateMovementTriggerAreaEntryRule() throws Exception {
        MovementDetails movementDetails = getMovementDetails();
        // AreaA
        movementDetails.setLongitude(1d);
        movementDetails.setLatitude(1d);

        CustomRule customRule = RulesTestHelper.createBasicCustomRule();
        List<RuleSegment> segments = new ArrayList<>();
        RuleSegment segment = new RuleSegment();
        segment.setCriteria("AREA");
        segment.setSubCriteria("AREA_CODE_ENT");
        segment.setCondition("EQ");
        segment.setValue("AreaA");
        segment.setLogicOperator("NONE");
        segment.setCustomRule(customRule);
        segment.setOrder(0);
        segments.add(segment);
        customRule.setRuleSegmentList(segments);
        rulesService.createCustomRule(customRule, "", "");

        customRulesEvaluator.evaluate(movementDetails);

        List<Ticket> tickets = rulesService.getTicketsByMovements(Arrays.asList(movementDetails.getMovementGuid()));
        assertThat(tickets.size(), is(1));
    }

    @Test
    @OperateOnDeployment("normal")
    public void evaluateMovementTriggerAreaExitRule() throws Exception {
        MovementDetails movementDetails = getMovementDetails();
        // AreaA
        movementDetails.setLongitude(1d);
        movementDetails.setLatitude(1d);
        // AreaB
        movementDetails.setPreviousLatitude(-1d);
        movementDetails.setPreviousLongitude(1d);

        CustomRule customRule = RulesTestHelper.createBasicCustomRule();
        List<RuleSegment> segments = new ArrayList<>();
        RuleSegment segment = new RuleSegment();
        segment.setCriteria("AREA");
        segment.setSubCriteria("AREA_CODE_EXT");
        segment.setCondition("EQ");
        segment.setValue("AreaB");
        segment.setLogicOperator("NONE");
        segment.setCustomRule(customRule);
        segment.setOrder(0);
        segments.add(segment);
        customRule.setRuleSegmentList(segments);
        rulesService.createCustomRule(customRule, "", "");

        customRulesEvaluator.evaluate(movementDetails);

        List<Ticket> tickets = rulesService.getTicketsByMovements(Arrays.asList(movementDetails.getMovementGuid()));
        assertThat(tickets.size(), is(1));
    }

    @Test
    @OperateOnDeployment("normal")
    public void evaluateMovementTriggerAreaEntRuleWithPrevousPosition() throws Exception {
        MovementDetails movementDetails = getMovementDetails();
        // AreaA
        movementDetails.setLongitude(1d);
        movementDetails.setLatitude(1d);
        // AreaB
        movementDetails.setPreviousLatitude(-1d);
        movementDetails.setPreviousLongitude(1d);

        CustomRule customRule = RulesTestHelper.createBasicCustomRule();
        List<RuleSegment> segments = new ArrayList<>();
        RuleSegment segment = new RuleSegment();
        segment.setCriteria("AREA");
        segment.setSubCriteria("AREA_CODE_ENT");
        segment.setCondition("EQ");
        segment.setValue("AreaA");
        segment.setLogicOperator("NONE");
        segment.setCustomRule(customRule);
        segment.setOrder(0);
        segments.add(segment);
        customRule.setRuleSegmentList(segments);
        rulesService.createCustomRule(customRule, "", "");

        customRulesEvaluator.evaluate(movementDetails);

        List<Ticket> tickets = rulesService.getTicketsByMovements(Arrays.asList(movementDetails.getMovementGuid()));
        assertThat(tickets.size(), is(1));
    }

    @Test
    @OperateOnDeployment("normal")
    public void evaluateMovementTriggerVMSAreaExitRule() throws Exception {
        MovementDetails movementDetails = getMovementDetails();
        // AreaA
        movementDetails.setLongitude(1d);
        movementDetails.setLatitude(1d);
        // AreaB
        movementDetails.setPreviousLatitude(-1d);
        movementDetails.setPreviousLongitude(1d);
        movementDetails.setPreviousVMSLatitude(-1d);
        movementDetails.setPreviousVMSLongitude(1d);

        CustomRule customRule = RulesTestHelper.createBasicCustomRule();
        List<RuleSegment> segments = new ArrayList<>();
        RuleSegment segment = new RuleSegment();
        segment.setCriteria("AREA");
        segment.setSubCriteria("AREA_CODE_VMS_EXT");
        segment.setCondition("EQ");
        segment.setValue("AreaB");
        segment.setLogicOperator("NONE");
        segment.setCustomRule(customRule);
        segment.setOrder(0);
        segments.add(segment);
        customRule.setRuleSegmentList(segments);
        rulesService.createCustomRule(customRule, "", "");

        customRulesEvaluator.evaluate(movementDetails);

        List<Ticket> tickets = rulesService.getTicketsByMovements(Arrays.asList(movementDetails.getMovementGuid()));
        assertThat(tickets.size(), is(1));
    }

    @Test
    @OperateOnDeployment("normal")
    public void evaluateMovementTriggerVMSAreaEntRuleWithPrevousPosition() throws Exception {
        MovementDetails movementDetails = getMovementDetails();
        // AreaA
        movementDetails.setLongitude(1d);
        movementDetails.setLatitude(1d);
        // AreaB
        movementDetails.setPreviousLatitude(-1d);
        movementDetails.setPreviousLongitude(1d);
        movementDetails.setPreviousVMSLatitude(-1d);
        movementDetails.setPreviousVMSLongitude(1d);

        CustomRule customRule = RulesTestHelper.createBasicCustomRule();
        List<RuleSegment> segments = new ArrayList<>();
        RuleSegment segment = new RuleSegment();
        segment.setCriteria("AREA");
        segment.setSubCriteria("AREA_CODE_VMS_ENT");
        segment.setCondition("EQ");
        segment.setValue("AreaA");
        segment.setLogicOperator("NONE");
        segment.setCustomRule(customRule);
        segment.setOrder(0);
        segments.add(segment);
        customRule.setRuleSegmentList(segments);
        rulesService.createCustomRule(customRule, "", "");

        customRulesEvaluator.evaluate(movementDetails);

        List<Ticket> tickets = rulesService.getTicketsByMovements(Arrays.asList(movementDetails.getMovementGuid()));
        assertThat(tickets.size(), is(1));
    }

    private MovementDetails getMovementDetails() {
        MovementDetails movementDetails = new MovementDetails();
        movementDetails.setMovementGuid(UUID.randomUUID().toString());
        movementDetails.setMobileTerminalGuid(UUID.randomUUID().toString());
        movementDetails.setLatitude(11d);
        movementDetails.setLongitude(56d);
        movementDetails.setPositionTime(Instant.now());
        movementDetails.setSource("INMARSAT_C");
        movementDetails.setAssetGuid(UUID.randomUUID().toString());
        movementDetails.setFlagState("SWE");
        return movementDetails;
    }
}
