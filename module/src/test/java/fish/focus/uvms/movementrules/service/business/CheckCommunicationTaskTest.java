package fish.focus.uvms.movementrules.service.business;

import fish.focus.uvms.config.service.ParameterService;
import fish.focus.uvms.movementrules.service.TransactionalTests;
import fish.focus.uvms.movementrules.service.bean.RulesServiceBean;
import fish.focus.uvms.movementrules.service.config.ParameterKey;
import fish.focus.uvms.movementrules.service.dao.RulesDao;
import fish.focus.uvms.movementrules.service.entity.PreviousReport;
import fish.focus.uvms.movementrules.service.message.JMSHelper;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.jms.TextMessage;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class CheckCommunicationTaskTest extends TransactionalTests {

    private static final long ONE_HOUR_IN_MILLISECONDS = 3600000;

    private static final String INCIDENT_EVENT = "IncidentEvent";

    private JMSHelper jmsHelper = new JMSHelper();

    @Inject
    RulesServiceBean rulesService;

    @Inject
    ParameterService parameterService;

    @Inject
    RulesDao rulesDao;

    @Before
    public void setThreshold() throws Exception {
        parameterService.setStringValue(ParameterKey.ASSET_NOT_SENDING_THRESHOLD.getKey(),
                String.valueOf(ONE_HOUR_IN_MILLISECONDS), "");

        System.clearProperty("AssetPollEndpointReached");

        jmsHelper.clearQueue(INCIDENT_EVENT);
    }

    @Test
    @OperateOnDeployment("normal")
    public void runTaskWithValidReport() throws Exception {
        PreviousReport previousReport = getBasicPreviousReport();
        rulesDao.updatePreviousReport(previousReport);

        new CheckCommunicationTask(rulesService, parameterService).run();

        TextMessage message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNull(message);
    }

    @Test
    @OperateOnDeployment("normal")
    public void runWithThresholdPassed() throws Exception {
        System.setProperty("AssetPollEndpointReached", "False");
        PreviousReport previousReport = getBasicPreviousReport();
        previousReport.setPositionTime(Instant.ofEpochMilli(System.currentTimeMillis() - ONE_HOUR_IN_MILLISECONDS));
        rulesDao.updatePreviousReport(previousReport);

        new CheckCommunicationTask(rulesService, parameterService).run();

        TextMessage message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNotNull(message);
    }

    @Test
    @OperateOnDeployment("normal")
    public void runTwiceWithThresholdPassed() throws Exception {
        PreviousReport previousReport = getBasicPreviousReport();
        previousReport.setPositionTime(Instant.ofEpochMilli(System.currentTimeMillis() - ONE_HOUR_IN_MILLISECONDS));
        rulesDao.updatePreviousReport(previousReport);

        CheckCommunicationTask checkCommunicationTask = new CheckCommunicationTask(rulesService, parameterService);
        checkCommunicationTask.run();
        TextMessage message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNotNull(message);

        checkCommunicationTask.run();
        message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNull(message);

    }

    @Test
    @OperateOnDeployment("normal")
    public void updateThresholdPassed() throws Exception {
        PreviousReport previousReport = getBasicPreviousReport();
        previousReport.setPositionTime(Instant.ofEpochMilli(System.currentTimeMillis() - ONE_HOUR_IN_MILLISECONDS));
        rulesDao.updatePreviousReport(previousReport);

        CheckCommunicationTask checkCommunicationTask = new CheckCommunicationTask(rulesService, parameterService);
        checkCommunicationTask.run();

        TextMessage message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNotNull(message);

        previousReport = rulesDao.getPreviousReportByAssetGuid(previousReport.getAssetGuid());
        previousReport.setPositionTime(Instant.ofEpochMilli(System.currentTimeMillis() - 3 * ONE_HOUR_IN_MILLISECONDS));
        previousReport.setUpdated(Instant.ofEpochMilli(System.currentTimeMillis() - 2 * ONE_HOUR_IN_MILLISECONDS));
        rulesDao.updatePreviousReport(previousReport);

        checkCommunicationTask.run();

        message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNotNull(message);
    }

    @Test
    @OperateOnDeployment("normal")
    public void checkPreviousReportUpdateTimeUpdated() throws Exception {
        PreviousReport previousReport = getBasicPreviousReport();
        previousReport.setPositionTime(Instant.ofEpochMilli(System.currentTimeMillis() - ONE_HOUR_IN_MILLISECONDS));
        rulesDao.updatePreviousReport(previousReport);

        CheckCommunicationTask checkCommunicationTask = new CheckCommunicationTask(rulesService, parameterService);
        checkCommunicationTask.run();

        PreviousReport fetchedReport = rulesDao.getPreviousReportByAssetGuid(previousReport.getAssetGuid());

        assertTrue(fetchedReport.getUpdated().isAfter(previousReport.getUpdated()));
    }

    @Test
    @OperateOnDeployment("normal")
    public void runTaskWith30MinSteps() throws Exception {
        long thirtyMinsInMs = ONE_HOUR_IN_MILLISECONDS / 2;
        PreviousReport previousReport = getBasicPreviousReport();
        rulesDao.updatePreviousReport(previousReport);

        CheckCommunicationTask checkCommunicationTask = new CheckCommunicationTask(rulesService, parameterService);
        checkCommunicationTask.run();

        TextMessage message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNull(message);

        // 30 mins
        previousReport = rulesDao.getPreviousReportByAssetGuid(previousReport.getAssetGuid());
        Instant positionTime = previousReport.getPositionTime();
        Instant updated = previousReport.getUpdated();
        previousReport.setPositionTime(positionTime.minus(30, ChronoUnit.MINUTES));
        previousReport.setUpdated(updated.minus(30, ChronoUnit.MINUTES));
        rulesDao.updatePreviousReport(previousReport);

        checkCommunicationTask.run();

        message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNull(message);

        // 1 hour
        previousReport = rulesDao.getPreviousReportByAssetGuid(previousReport.getAssetGuid());
        positionTime = previousReport.getPositionTime();
        updated = previousReport.getUpdated();
        previousReport.setPositionTime(positionTime.minus(30, ChronoUnit.MINUTES));
        previousReport.setUpdated(updated.minus(30, ChronoUnit.MINUTES));
        rulesDao.updatePreviousReport(previousReport);

        checkCommunicationTask.run();

        message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNotNull(message);

        // 1.5 hour
        previousReport = rulesDao.getPreviousReportByAssetGuid(previousReport.getAssetGuid());
        positionTime = previousReport.getPositionTime();
        updated = previousReport.getUpdated();
        previousReport.setPositionTime(positionTime.minus(30, ChronoUnit.MINUTES));
        previousReport.setUpdated(updated.minus(30, ChronoUnit.MINUTES));
        rulesDao.updatePreviousReport(previousReport);

        checkCommunicationTask.run();

        message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNull(message);

        // 2 hours
        previousReport = rulesDao.getPreviousReportByAssetGuid(previousReport.getAssetGuid());
        positionTime = previousReport.getPositionTime();
        updated = previousReport.getUpdated();
        previousReport.setPositionTime(positionTime.minus(30, ChronoUnit.MINUTES));
        previousReport.setUpdated(updated.minus(30, ChronoUnit.MINUTES));
        rulesDao.updatePreviousReport(previousReport);

        checkCommunicationTask.run();

        message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNotNull(message);

        // 2.5 hours
        previousReport = rulesDao.getPreviousReportByAssetGuid(previousReport.getAssetGuid());
        positionTime = previousReport.getPositionTime();
        updated = previousReport.getUpdated();
        previousReport.setPositionTime(positionTime.minus(30, ChronoUnit.MINUTES));
        previousReport.setUpdated(updated.minus(30, ChronoUnit.MINUTES));
        rulesDao.updatePreviousReport(previousReport);

        checkCommunicationTask.run();

        message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNull(message);
    }

    @Test
    @OperateOnDeployment("normal")
    public void runTaskWith30MinStepsWithPastPositionTime() throws Exception {
        long thirtyMinsInMs = ONE_HOUR_IN_MILLISECONDS / 2;
        PreviousReport previousReport = getBasicPreviousReport();
        previousReport.setPositionTime(Instant.ofEpochMilli(System.currentTimeMillis() - thirtyMinsInMs));
        rulesDao.updatePreviousReport(previousReport);

        CheckCommunicationTask checkCommunicationTask = new CheckCommunicationTask(rulesService, parameterService);
        checkCommunicationTask.run();

        TextMessage message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNull(message);

        // 1 hour
        previousReport = rulesDao.getPreviousReportByAssetGuid(previousReport.getAssetGuid());
        Instant positionTime = previousReport.getPositionTime();
        Instant updated = previousReport.getUpdated();
        previousReport.setPositionTime(positionTime.minus(30, ChronoUnit.MINUTES));
        previousReport.setUpdated(updated.minus(30, ChronoUnit.MINUTES));
        rulesDao.updatePreviousReport(previousReport);

        checkCommunicationTask.run();

        message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNotNull(message);

        // 1.5 hour
        previousReport = rulesDao.getPreviousReportByAssetGuid(previousReport.getAssetGuid());
        positionTime = previousReport.getPositionTime();
        updated = previousReport.getUpdated();
        previousReport.setPositionTime(positionTime.minus(30, ChronoUnit.MINUTES));
        previousReport.setUpdated(updated.minus(30, ChronoUnit.MINUTES));
        rulesDao.updatePreviousReport(previousReport);

        checkCommunicationTask.run();

        message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNull(message);

        // 2 hour
        previousReport = rulesDao.getPreviousReportByAssetGuid(previousReport.getAssetGuid());
        positionTime = previousReport.getPositionTime();
        updated = previousReport.getUpdated();
        previousReport.setPositionTime(positionTime.minus(30, ChronoUnit.MINUTES));
        previousReport.setUpdated(updated.minus(30, ChronoUnit.MINUTES));
        rulesDao.updatePreviousReport(previousReport);

        checkCommunicationTask.run();

        message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNotNull(message);

        // 2.5 hours
        previousReport = rulesDao.getPreviousReportByAssetGuid(previousReport.getAssetGuid());
        positionTime = previousReport.getPositionTime();
        updated = previousReport.getUpdated();
        previousReport.setPositionTime(positionTime.minus(30, ChronoUnit.MINUTES));
        previousReport.setUpdated(updated.minus(30, ChronoUnit.MINUTES));
        rulesDao.updatePreviousReport(previousReport);

        checkCommunicationTask.run();

        message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNull(message);

        // 3 hours
        previousReport = rulesDao.getPreviousReportByAssetGuid(previousReport.getAssetGuid());
        positionTime = previousReport.getPositionTime();
        updated = previousReport.getUpdated();
        previousReport.setPositionTime(positionTime.minus(30, ChronoUnit.MINUTES));
        previousReport.setUpdated(updated.minus(30, ChronoUnit.MINUTES));
        rulesDao.updatePreviousReport(previousReport);

        checkCommunicationTask.run();

        message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNotNull(message);
    }

    private PreviousReport getBasicPreviousReport() {
        PreviousReport previousReport = new PreviousReport();

        previousReport.setPositionTime(Instant.now());
        previousReport.setAssetGuid(UUID.randomUUID().toString());
        previousReport.setMovementGuid(UUID.randomUUID());
        previousReport.setMobTermGuid(UUID.randomUUID());
        previousReport.setUpdated(Instant.now());
        previousReport.setUpdatedBy("UVMS");

        return previousReport;
    }
}
