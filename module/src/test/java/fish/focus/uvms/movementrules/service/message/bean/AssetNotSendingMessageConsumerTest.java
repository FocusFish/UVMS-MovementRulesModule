package fish.focus.uvms.movementrules.service.message.bean;

import fish.focus.uvms.movementrules.model.dto.MovementDetails;
import fish.focus.uvms.movementrules.service.BuildRulesServiceDeployment;
import fish.focus.uvms.movementrules.service.bean.CustomRulesEvaluator;
import fish.focus.uvms.movementrules.service.bean.RulesServiceBean;
import fish.focus.uvms.movementrules.service.business.RulesValidator;
import fish.focus.uvms.movementrules.service.constants.ServiceConstants;
import fish.focus.uvms.movementrules.service.dao.RulesDao;
import fish.focus.uvms.movementrules.service.entity.PreviousReport;
import fish.focus.uvms.movementrules.service.message.JMSHelper;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.time.Instant;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class AssetNotSendingMessageConsumerTest extends BuildRulesServiceDeployment {

    private static final String QUEUE_NAME = "IncidentEvent";

    @Inject
    private RulesServiceBean rulesService;

    @Inject
    private RulesValidator rulesValidator;

    @Inject
    private RulesDao rulesDao;

    @Inject
    private CustomRulesEvaluator customRulesEvaluator;

    private JMSHelper jmsHelper = new JMSHelper();

    @Before
    public void clearExchangeQueue() throws Exception {
        jmsHelper.clearQueue(QUEUE_NAME);
    }

    @Test
    @OperateOnDeployment("normal")
    public void createAssetNotSendingEventAndConsumeIncidentQueue() throws Exception {
        rulesValidator.updateCustomRules();

        MovementDetails movementDetails = getMovementDetails();
        PreviousReport report = new PreviousReport();
        report.setAssetGuid(movementDetails.getAssetGuid());
        report.setMovementGuid(UUID.fromString(movementDetails.getMovementGuid()));
        report.setMobTermGuid(UUID.fromString(movementDetails.getMobileTerminalGuid()));
        rulesService.timerRuleTriggered(ServiceConstants.ASSET_NOT_SENDING_RULE, report);


        // AssetNotSending Create Event
        Message message1 = jmsHelper.listenOnQueue(QUEUE_NAME);
        assertNotNull(message1);

        // Run next movement
        customRulesEvaluator.evaluate(movementDetails);

        // AssetNotSending Update Event
        Message message2 = jmsHelper.listenOnQueue(QUEUE_NAME);
        assertNotNull(message2);
    }

    @Test
    @OperateOnDeployment("normal")
    public void createAssetSendingDespiteBeingParkedTest() throws Exception {
        rulesValidator.updateCustomRules();

        MovementDetails movementDetails = getMovementDetails();
        movementDetails.setParked(true);
        customRulesEvaluator.evaluate(movementDetails);

        // sending despite Create Event
        TextMessage message = (TextMessage) jmsHelper.listenOnQueue(QUEUE_NAME);
        assertNotNull(message);

        assertTrue(message.getText().contains(movementDetails.getAssetGuid()));
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
