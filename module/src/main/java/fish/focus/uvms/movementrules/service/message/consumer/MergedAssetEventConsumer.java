package fish.focus.uvms.movementrules.service.message.consumer;

import fish.focus.uvms.asset.remote.dto.AssetMergeInfo;
import fish.focus.uvms.commons.date.JsonBConfigurator;
import fish.focus.uvms.movementrules.service.dao.RulesDao;
import fish.focus.uvms.movementrules.service.entity.PreviousReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.json.bind.Jsonb;

import static fish.focus.uvms.commons.message.api.MessageConstants.*;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = DESTINATION_TYPE_TOPIC),
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = EVENT_STREAM_TOPIC),
        @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = DURABLE_CONNECTION),
        @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "movement-rules-merged-asset"),
        @ActivationConfigProperty(propertyName = "clientId", propertyValue = "movement-rules-merged-asset"),
        @ActivationConfigProperty(propertyName = "messageSelector", propertyValue = EVENT_STREAM_EVENT + "='Merged Asset'")
})
public class MergedAssetEventConsumer implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(MergedAssetEventConsumer.class);

    @Inject
    private RulesDao rulesDao;

    private Jsonb jsonb;

    @PostConstruct
    public void init() {
        jsonb = new JsonBConfigurator().getContext(null);
    }

    @Override
    public void onMessage(Message message) {
        try {
            TextMessage textMessage = (TextMessage) message;
            AssetMergeInfo asset = jsonb.fromJson(textMessage.getText(), AssetMergeInfo.class);

            PreviousReport previousReport = rulesDao.getPreviousReportByAssetGuid(asset.getOldAssetId());

            if (previousReport == null) {
                // don't need to do anything since the asset hadn't been added to the previous report table
                return;
            }

            LOG.info("Asset got new ID ({}) -> deleting previous report for asset with old ID {}", asset.getNewAssetId(), asset.getOldAssetId());
            rulesDao.deletePreviousReport(previousReport);
        } catch (JMSException e) {
            LOG.error("Could not handle Merged Asset event message", e);
        }
    }
}
