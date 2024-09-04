/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package fish.focus.uvms.movementrules.service.business;

import fish.focus.uvms.asset.client.AssetClient;
import fish.focus.uvms.asset.client.model.AssetDTO;
import fish.focus.uvms.asset.client.model.AssetIdentifier;
import fish.focus.uvms.config.service.ParameterService;
import fish.focus.uvms.movementrules.service.bean.RulesServiceBean;
import fish.focus.uvms.movementrules.service.config.ParameterKey;
import fish.focus.uvms.movementrules.service.constants.ServiceConstants;
import fish.focus.uvms.movementrules.service.dao.RulesDao;
import fish.focus.uvms.movementrules.service.entity.PreviousReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import java.time.Instant;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

@Singleton
public class CheckCommunicationTask {
    private static final long TWO_HOURS_IN_MILLISECONDS = 7200000;

    private static final Logger LOG = LoggerFactory.getLogger(CheckCommunicationTask.class);

    private RulesServiceBean rulesService;
    private ParameterService parameterService;
    private AssetClient assetClient;
    private RulesDao rulesDao;

    public CheckCommunicationTask() {
    }

    @Inject
    public CheckCommunicationTask(RulesServiceBean rulesService, ParameterService parameterService, AssetClient assetClient, RulesDao rulesDao) {
        this.rulesService = rulesService;
        this.parameterService = parameterService;
        this.assetClient = assetClient;
        this.rulesDao = rulesDao;
    }

    @Schedule(minute = "*/10", hour = "*", persistent = false)
    public void runAssetNotSendingRule() {
        try {
            LOG.debug("Check for non-sending assets");
            // Get all previous reports from DB
            List<PreviousReport> previousReports = rulesService.getPreviousMovementReports();
            long threshold = getAssetNotSendingThreshold();

            for (PreviousReport previousReport : previousReports) {
                handlePreviousReport(previousReport, threshold);
            }
        } catch (Exception e) {
            LOG.error("Could not execute 'Asset not sending' rule", e);
        }
    }

    private boolean isThresholdPassed(Instant positionTime, Instant lastUpdated, long threshold) {
        long positionThreshold = positionTime.toEpochMilli() + threshold;
        long updateThreshold = lastUpdated.toEpochMilli() + threshold;
        long now = System.currentTimeMillis();
        return positionThreshold <= now && (lastUpdated.toEpochMilli() <= positionThreshold || updateThreshold <= now);
    }

    private long getAssetNotSendingThreshold() {
        try {
            String thresholdSetting = parameterService.getStringValue(ParameterKey.ASSET_NOT_SENDING_THRESHOLD.getKey());
            return Long.parseLong(thresholdSetting);
        } catch (Exception e) {
            LOG.error("Unable to get asset not sending threshold from parameter service due to {}. Returning two hours instead: ", e.getMessage(), e);
            return TWO_HOURS_IN_MILLISECONDS;
        }
    }

    private void handlePreviousReport(PreviousReport previousReport, long threshold) {
        Instant positionTime = previousReport.getPositionTime();
        Instant lastUpdated = previousReport.getUpdated();

        if (!isThresholdPassed(positionTime, lastUpdated, threshold)) {
            return;
        }

        AssetDTO asset = assetClient.getAssetById(AssetIdentifier.GUID, previousReport.getAssetGuid());
        if (asset == null || FALSE.equals(asset.getActive()) || TRUE.equals(asset.isParked())) {
            // asset either doesn't exist or is inactive / parked
            // there should have been an 'Updated Asset' event that EventConsumer should have
            // picked up on and subsequently removed the previousReport entry. Do that now instead.
            String cause = getCause(asset);
            LOG.warn("Removing previousReport with id {} for asset {} since it {}", previousReport.getId(), previousReport.getAssetGuid(), cause);
            rulesDao.deletePreviousReport(previousReport);
            return;
        }

        sendIncidentMessage(previousReport, threshold, positionTime);
    }

    private static String getCause(AssetDTO asset) {
        if (asset == null) {
            return "doesn't exist";
        }

        return FALSE.equals(asset.getActive()) ? "is not active" : "is parked";
    }

    private void sendIncidentMessage(PreviousReport previousReport, long threshold, Instant positionTime) {
        previousReport.setUpdated(Instant.now());

        LOG.info("\t ==> Executing RULE '{}', assetGuid: {}, positionTime: {}, threshold: {}",
                ServiceConstants.ASSET_NOT_SENDING_RULE, previousReport.getAssetGuid(), positionTime, threshold);

        String ruleName = ServiceConstants.ASSET_NOT_SENDING_RULE;
        rulesService.timerRuleTriggered(ruleName, previousReport);
    }
}
