package fish.focus.uvms.movementrules.service.message;

import fish.focus.uvms.asset.remote.dto.AssetMergeInfo;
import fish.focus.uvms.commons.date.JsonBConfigurator;
import fish.focus.uvms.movementrules.service.BuildRulesServiceDeployment;
import fish.focus.uvms.movementrules.service.dao.RulesDao;
import fish.focus.uvms.movementrules.service.entity.PreviousReport;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.json.bind.Jsonb;
import java.time.Instant;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.with;
import static org.hamcrest.CoreMatchers.*;

@RunWith(Arquillian.class)
public class MergedAssetEventConsumerTest extends BuildRulesServiceDeployment {

    public static final String MERGED_ASSET = "Merged Asset";

    private final Jsonb jsonb = new JsonBConfigurator().getContext(null);

    @Inject
    private RulesDao rulesDao;

    @Inject
    private JMSHelper jmsHelper;

    @Test
    @OperateOnDeployment("normal")
    public void noOperationWhenNoPreviousReport() {
        String oldUuid = UUID.randomUUID().toString();
        // shouldn't have any entries in the previous report table
        await().until(() -> rulesDao.getPreviousReportByAssetGuid(oldUuid), is(nullValue()));

        String newUuid = UUID.randomUUID().toString();
        AssetMergeInfo assetMergeInfo = new AssetMergeInfo(oldUuid, newUuid);

        String assetMergedMessage = jsonb.toJson(assetMergeInfo);
        jmsHelper.sendMessageOnEventStream(assetMergedMessage, MERGED_ASSET);

        with().pollDelay(200, MILLISECONDS).await()
                .until(() -> rulesDao.getPreviousReportByAssetGuid(oldUuid), is(nullValue()));
    }

    @Test
    @OperateOnDeployment("normal")
    public void removePreviousReportOnMergedAssetEvent() {
        String oldUuid = UUID.randomUUID().toString();

        PreviousReport previousReport = getBasicPreviousReport(oldUuid);
        rulesDao.updatePreviousReport(previousReport);
        await().until(() -> rulesDao.getPreviousReportByAssetGuid(oldUuid), is(notNullValue()));

        String newUuid = UUID.randomUUID().toString();
        AssetMergeInfo assetMergeInfo = new AssetMergeInfo(oldUuid, newUuid);

        String assetMergedMessage = jsonb.toJson(assetMergeInfo);
        jmsHelper.sendMessageOnEventStream(assetMergedMessage, MERGED_ASSET);

        await().until(() -> rulesDao.getPreviousReportByAssetGuid(oldUuid), is(nullValue()));
    }

    private static PreviousReport getBasicPreviousReport(String oldUuid) {
        PreviousReport previousReport = new PreviousReport();

        previousReport.setAssetGuid(oldUuid);
        previousReport.setMobTermGuid(UUID.randomUUID());
        previousReport.setMovementGuid(UUID.randomUUID());
        previousReport.setPositionTime(Instant.now());
        previousReport.setUpdated(Instant.now());
        previousReport.setUpdatedBy(MergedAssetEventConsumerTest.class.getSimpleName());

        return previousReport;
    }
}
