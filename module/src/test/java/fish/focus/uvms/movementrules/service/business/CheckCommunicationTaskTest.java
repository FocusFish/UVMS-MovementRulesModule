package fish.focus.uvms.movementrules.service.business;

import fish.focus.uvms.asset.client.AssetClient;
import fish.focus.uvms.asset.client.model.AssetDTO;
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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.inject.Inject;
import javax.jms.TextMessage;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(Arquillian.class)
public class CheckCommunicationTaskTest extends TransactionalTests {

    private static final long ONE_HOUR_IN_MILLISECONDS = 3600000;

    private static final String INCIDENT_EVENT = "IncidentEvent";

    @Inject
    ParameterService parameterService;

    @Mock
    AssetClient assetClient;

    @Inject
    RulesServiceBean rulesServiceBean;

    @Inject
    RulesDao rulesDao;

    private CheckCommunicationTask checkCommunicationTask;
    private final JMSHelper jmsHelper = new JMSHelper();

    @Before
    public void setThreshold() throws Exception {
        parameterService.setStringValue(ParameterKey.ASSET_NOT_SENDING_THRESHOLD.getKey(),
                String.valueOf(ONE_HOUR_IN_MILLISECONDS), "");

        System.clearProperty("AssetPollEndpointReached");

        jmsHelper.clearQueue(INCIDENT_EVENT);
    }

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        checkCommunicationTask = new CheckCommunicationTask(rulesServiceBean, parameterService, assetClient, rulesDao);
    }

    @Test
    @OperateOnDeployment("normal")
    public void runTaskWithValidReport() throws Exception {
        PreviousReport previousReport = getBasicPreviousReport();
        rulesDao.updatePreviousReport(previousReport);
        var assetDto = getBasicAsset(previousReport.getAssetGuid());
        when(assetClient.getAssetById(any(), any())).thenReturn(assetDto);

        checkCommunicationTask.runAssetNotSendingRule();

        TextMessage message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNull(message);
    }

    @Test
    @OperateOnDeployment("normal")
    public void runWithThresholdPassed() throws Exception {
        System.setProperty("AssetPollEndpointReached", "False");
        PreviousReport previousReport = getBasicPreviousReport();
        Instant oneHourAgo = Instant.now().minus(1, HOURS);
        previousReport.setPositionTime(oneHourAgo);
        rulesDao.updatePreviousReport(previousReport);
        var assetDto = getBasicAsset(previousReport.getAssetGuid());
        when(assetClient.getAssetById(any(), any())).thenReturn(assetDto);

        checkCommunicationTask.runAssetNotSendingRule();

        TextMessage message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNotNull(message);
    }

    @Test
    @OperateOnDeployment("normal")
    public void runWithThresholdPassedAndInactiveAsset() {
        PreviousReport previousReport = getBasicPreviousReport();
        Instant oneHourAgo = Instant.now().minus(1, HOURS);
        previousReport.setPositionTime(oneHourAgo);
        rulesDao.updatePreviousReport(previousReport);
        var assetDto = getBasicAsset(previousReport.getAssetGuid());
        assetDto.setActive(false);
        when(assetClient.getAssetById(any(), any())).thenReturn(assetDto);

        checkCommunicationTask.runAssetNotSendingRule();

        PreviousReport noPreviousReport = rulesDao.getPreviousReportByAssetGuid(assetDto.getId().toString());
        assertThat("Should be no previous report", noPreviousReport, is(nullValue()));
    }

    @Test
    @OperateOnDeployment("normal")
    public void runWithThresholdPassedAndParkedAsset() {
        PreviousReport previousReport = getBasicPreviousReport();
        Instant oneHourAgo = Instant.now().minus(1, HOURS);
        previousReport.setPositionTime(oneHourAgo);
        rulesDao.updatePreviousReport(previousReport);
        var assetDto = getBasicAsset(previousReport.getAssetGuid());
        assetDto.setParked(true);
        when(assetClient.getAssetById(any(), any())).thenReturn(assetDto);

        checkCommunicationTask.runAssetNotSendingRule();

        PreviousReport noPreviousReport = rulesDao.getPreviousReportByAssetGuid(assetDto.getId().toString());
        assertThat("Should be no previous report", noPreviousReport, is(nullValue()));
    }

    @Test
    @OperateOnDeployment("normal")
    public void runTwiceWithThresholdPassed() throws Exception {
        PreviousReport previousReport = getBasicPreviousReport();
        Instant oneHourAgo = Instant.now().minus(1, HOURS);
        previousReport.setPositionTime(oneHourAgo);
        rulesDao.updatePreviousReport(previousReport);
        var assetDto = getBasicAsset(previousReport.getAssetGuid());
        when(assetClient.getAssetById(any(), any())).thenReturn(assetDto);

        checkCommunicationTask.runAssetNotSendingRule();
        TextMessage message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNotNull(message);

        checkCommunicationTask.runAssetNotSendingRule();
        message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNull(message);
    }

    @Test
    @OperateOnDeployment("normal")
    public void updateThresholdPassed() throws Exception {
        PreviousReport previousReport = getBasicPreviousReport();
        Instant oneHourAgo = Instant.now().minus(1, HOURS);
        previousReport.setPositionTime(oneHourAgo);
        rulesDao.updatePreviousReport(previousReport);
        var assetDto = getBasicAsset(previousReport.getAssetGuid());
        when(assetClient.getAssetById(any(), any())).thenReturn(assetDto);

        checkCommunicationTask.runAssetNotSendingRule();

        TextMessage message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNotNull(message);

        previousReport = rulesDao.getPreviousReportByAssetGuid(previousReport.getAssetGuid());
        Instant threeHoursAgo = Instant.now().minus(3, HOURS);
        previousReport.setPositionTime(threeHoursAgo);
        Instant twoHoursAgo = Instant.now().minus(1, HOURS);
        previousReport.setUpdated(twoHoursAgo);
        rulesDao.updatePreviousReport(previousReport);

        checkCommunicationTask.runAssetNotSendingRule();

        message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNotNull(message);
    }

    @Test
    @OperateOnDeployment("normal")
    public void checkPreviousReportUpdateTimeUpdated() {
        PreviousReport previousReport = getBasicPreviousReport();
        Instant oneHourAgo = Instant.now().minus(1, HOURS);
        previousReport.setPositionTime(oneHourAgo);
        rulesDao.updatePreviousReport(previousReport);
        var assetDto = getBasicAsset(previousReport.getAssetGuid());
        when(assetClient.getAssetById(any(), any())).thenReturn(assetDto);

        checkCommunicationTask.runAssetNotSendingRule();

        PreviousReport fetchedReport = rulesDao.getPreviousReportByAssetGuid(previousReport.getAssetGuid());

        assertTrue(fetchedReport.getUpdated().isAfter(previousReport.getUpdated()));
    }

    @Test
    @OperateOnDeployment("normal")
    public void runTaskWith30MinSteps() throws Exception {
        PreviousReport previousReport = getBasicPreviousReport();
        rulesDao.updatePreviousReport(previousReport);
        var assetDto = getBasicAsset(previousReport.getAssetGuid());
        when(assetClient.getAssetById(any(), any())).thenReturn(assetDto);

        checkCommunicationTask.runAssetNotSendingRule();

        TextMessage message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNull(message);

        // 30 mins
        previousReport = rulesDao.getPreviousReportByAssetGuid(previousReport.getAssetGuid());
        Instant positionTime = previousReport.getPositionTime();
        Instant updated = previousReport.getUpdated();
        previousReport.setPositionTime(positionTime.minus(30, ChronoUnit.MINUTES));
        previousReport.setUpdated(updated.minus(30, ChronoUnit.MINUTES));
        rulesDao.updatePreviousReport(previousReport);

        checkCommunicationTask.runAssetNotSendingRule();

        message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNull(message);

        // 1 hour
        previousReport = rulesDao.getPreviousReportByAssetGuid(previousReport.getAssetGuid());
        positionTime = previousReport.getPositionTime();
        updated = previousReport.getUpdated();
        previousReport.setPositionTime(positionTime.minus(30, ChronoUnit.MINUTES));
        previousReport.setUpdated(updated.minus(30, ChronoUnit.MINUTES));
        rulesDao.updatePreviousReport(previousReport);

        checkCommunicationTask.runAssetNotSendingRule();

        message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNotNull(message);

        // 1.5 hour
        previousReport = rulesDao.getPreviousReportByAssetGuid(previousReport.getAssetGuid());
        positionTime = previousReport.getPositionTime();
        updated = previousReport.getUpdated();
        previousReport.setPositionTime(positionTime.minus(30, ChronoUnit.MINUTES));
        previousReport.setUpdated(updated.minus(30, ChronoUnit.MINUTES));
        rulesDao.updatePreviousReport(previousReport);

        checkCommunicationTask.runAssetNotSendingRule();

        message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNull(message);

        // 2 hours
        previousReport = rulesDao.getPreviousReportByAssetGuid(previousReport.getAssetGuid());
        positionTime = previousReport.getPositionTime();
        updated = previousReport.getUpdated();
        previousReport.setPositionTime(positionTime.minus(30, ChronoUnit.MINUTES));
        previousReport.setUpdated(updated.minus(30, ChronoUnit.MINUTES));
        rulesDao.updatePreviousReport(previousReport);

        checkCommunicationTask.runAssetNotSendingRule();

        message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNotNull(message);

        // 2.5 hours
        previousReport = rulesDao.getPreviousReportByAssetGuid(previousReport.getAssetGuid());
        positionTime = previousReport.getPositionTime();
        updated = previousReport.getUpdated();
        previousReport.setPositionTime(positionTime.minus(30, ChronoUnit.MINUTES));
        previousReport.setUpdated(updated.minus(30, ChronoUnit.MINUTES));
        rulesDao.updatePreviousReport(previousReport);

        checkCommunicationTask.runAssetNotSendingRule();

        message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNull(message);
    }

    @Test
    @OperateOnDeployment("normal")
    public void runTaskWith30MinStepsWithPastPositionTime() throws Exception {
        PreviousReport previousReport = getBasicPreviousReport();
        previousReport.setPositionTime(Instant.now().minus(30, MINUTES));
        rulesDao.updatePreviousReport(previousReport);
        var assetDto = getBasicAsset(previousReport.getAssetGuid());
        when(assetClient.getAssetById(any(), any())).thenReturn(assetDto);

        checkCommunicationTask.runAssetNotSendingRule();

        TextMessage message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNull(message);

        // 1 hour
        previousReport = rulesDao.getPreviousReportByAssetGuid(previousReport.getAssetGuid());
        Instant positionTime = previousReport.getPositionTime();
        Instant updated = previousReport.getUpdated();
        previousReport.setPositionTime(positionTime.minus(30, ChronoUnit.MINUTES));
        previousReport.setUpdated(updated.minus(30, ChronoUnit.MINUTES));
        rulesDao.updatePreviousReport(previousReport);

        checkCommunicationTask.runAssetNotSendingRule();

        message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNotNull(message);

        // 1.5 hour
        previousReport = rulesDao.getPreviousReportByAssetGuid(previousReport.getAssetGuid());
        positionTime = previousReport.getPositionTime();
        updated = previousReport.getUpdated();
        previousReport.setPositionTime(positionTime.minus(30, ChronoUnit.MINUTES));
        previousReport.setUpdated(updated.minus(30, ChronoUnit.MINUTES));
        rulesDao.updatePreviousReport(previousReport);

        checkCommunicationTask.runAssetNotSendingRule();

        message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNull(message);

        // 2 hour
        previousReport = rulesDao.getPreviousReportByAssetGuid(previousReport.getAssetGuid());
        positionTime = previousReport.getPositionTime();
        updated = previousReport.getUpdated();
        previousReport.setPositionTime(positionTime.minus(30, ChronoUnit.MINUTES));
        previousReport.setUpdated(updated.minus(30, ChronoUnit.MINUTES));
        rulesDao.updatePreviousReport(previousReport);

        checkCommunicationTask.runAssetNotSendingRule();

        message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNotNull(message);

        // 2.5 hours
        previousReport = rulesDao.getPreviousReportByAssetGuid(previousReport.getAssetGuid());
        positionTime = previousReport.getPositionTime();
        updated = previousReport.getUpdated();
        previousReport.setPositionTime(positionTime.minus(30, ChronoUnit.MINUTES));
        previousReport.setUpdated(updated.minus(30, ChronoUnit.MINUTES));
        rulesDao.updatePreviousReport(previousReport);

        checkCommunicationTask.runAssetNotSendingRule();

        message = (TextMessage) jmsHelper.listenOnQueue(INCIDENT_EVENT);
        assertNull(message);

        // 3 hours
        previousReport = rulesDao.getPreviousReportByAssetGuid(previousReport.getAssetGuid());
        positionTime = previousReport.getPositionTime();
        updated = previousReport.getUpdated();
        previousReport.setPositionTime(positionTime.minus(30, ChronoUnit.MINUTES));
        previousReport.setUpdated(updated.minus(30, ChronoUnit.MINUTES));
        rulesDao.updatePreviousReport(previousReport);

        checkCommunicationTask.runAssetNotSendingRule();

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

    private AssetDTO getBasicAsset(String assetGuid) {
        AssetDTO asset = new AssetDTO();

        asset.setId(UUID.fromString(assetGuid));
        asset.setName("Test asset");
        asset.setActive(true);
        asset.setExternalMarking("EXT123");
        asset.setFlagStateCode("SWE");

        asset.setCommissionDate(Instant.now());
        asset.setCfr("CRF" + getRandomIntegers(9));
        asset.setIrcs("F" + getRandomIntegers(7));
        asset.setImo(getRandomIntegers(7));
        asset.setMmsi("M" + getRandomIntegers(8));
        asset.setIccat("ICCAT" + getRandomIntegers(20));
        asset.setUvi("UVI" + getRandomIntegers(20));
        asset.setGfcm("GFCM" + getRandomIntegers(20));

        asset.setGrossTonnage(10d);
        asset.setPowerOfMainEngine(10d);

        asset.setGearFishingType("Demersal");

        asset.setOwnerName("Foo Bar");
        asset.setOwnerAddress("Hacker st. 1337");

        asset.setProdOrgCode("ORGCODE");
        asset.setProdOrgName("ORGNAME");

        asset.setUpdateTime(Instant.now());
        asset.setUpdatedBy("TEST");

        return asset;
    }

    private String getRandomIntegers(int length) {
        return new Random()
                .ints(0, 9)
                .mapToObj(String::valueOf)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }
}
