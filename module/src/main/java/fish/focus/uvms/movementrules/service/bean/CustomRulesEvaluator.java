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
package fish.focus.uvms.movementrules.service.bean;

import fish.focus.schema.movementrules.movement.v1.MovementSourceType;
import fish.focus.uvms.config.exception.ConfigServiceException;
import fish.focus.uvms.config.service.ParameterService;
import fish.focus.uvms.movementrules.model.dto.MovementDetails;
import fish.focus.uvms.movementrules.service.boundary.SpatialRestClient;
import fish.focus.uvms.movementrules.service.business.RulesValidator;
import fish.focus.uvms.movementrules.service.config.ParameterKey;
import fish.focus.uvms.movementrules.service.dao.RulesDao;
import fish.focus.uvms.movementrules.service.dto.EventTicket;
import fish.focus.uvms.movementrules.service.entity.PreviousReport;
import fish.focus.uvms.movementrules.service.event.TicketUpdateEvent;
import fish.focus.uvms.movementrules.service.message.producer.bean.IncidentProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.time.Instant;
import java.util.UUID;

@Stateless
public class CustomRulesEvaluator {

    private static final Logger LOG = LoggerFactory.getLogger(CustomRulesEvaluator.class);

    private String localFlagstate;

    @Inject
    private RulesValidator rulesValidator;

    @EJB
    private ParameterService parameterService;

    @Inject
    private SpatialRestClient spatialClient;

    @Inject
    private RulesDao rulesDao;

    @Inject
    @TicketUpdateEvent
    private Event<EventTicket> ticketUpdateEvent;

    @Inject
    private RulesServiceBean rulesServiceBean;

    @Inject
    private IncidentProducer incidentProducer;

    public void evaluate(MovementDetails movementDetails) {
        Long timeDiffPositionReport = timeDiffAndPersistPreviousReport(movementDetails);

        movementDetails.setTimeDiffPositionReport(timeDiffPositionReport);

        sendPositionToIncident(movementDetails);

        spatialClient.populateAreasAndAreaTransitions(movementDetails);

        rulesValidator.evaluate(movementDetails);
    }

    private Long timeDiffAndPersistPreviousReport(MovementDetails movementDetails) {
        String movementSource = movementDetails.getSource();
        String assetGuid = movementDetails.getAssetGuid();
        String movementId = movementDetails.getMovementGuid();
        String assetFlagState = movementDetails.getFlagState();
        Instant positionTime = movementDetails.getPositionTime();

        // This needs to be done before persisting last report
        Long timeDiffInSeconds = null;
        Long timeDiff = timeDiffFromLastCommunication(assetGuid, positionTime);
        timeDiffInSeconds = timeDiff != null ? timeDiff / 1000 : null;

        // We only persist our own last communications that were from Inmarsat or Iridium.
        if (isLocalFlagState(assetFlagState) &&
                (movementSource.equals(MovementSourceType.INMARSAT_C.value())
                        || movementSource.equals(MovementSourceType.IRIDIUM.value()))) {
            persistLastCommunication(movementDetails);
        }

        return timeDiffInSeconds;
    }

    private void sendPositionToIncident(MovementDetails movementDetails) {
        if (shouldPositionBeSentToIncident(movementDetails)) {
            incidentProducer.sendPositionToIncident(movementDetails);
        }
    }

    private boolean shouldPositionBeSentToIncident(MovementDetails movementDetails) {
        return (movementDetails.isParked()
                || (!movementDetails.getSource().equals(MovementSourceType.AIS.value()) && isLocalFlagState(movementDetails.getFlagState())));
    }

    private Long timeDiffFromLastCommunication(String assetGuid, Instant thisTime) {
        Long timeDiff = null;
        try {
            PreviousReport entity = rulesDao.getPreviousReportByAssetGuid(assetGuid);
            if (entity == null) {         //aka not local flag state and not AIS, see line 93
                return null;
            }

            Instant previousTime = entity.getPositionTime();
            timeDiff = thisTime.toEpochMilli() - previousTime.toEpochMilli();
        } catch (Exception e) {
            // If something goes wrong, continue with the other validation
            LOG.error("[ERROR] Error when getting previous report by asset guid {}", e.getMessage());
        }
        return timeDiff;
    }

    private void persistLastCommunication(MovementDetails movementDetails) {
        String assetGuid = movementDetails.getAssetGuid();
        String movementId = movementDetails.getMovementGuid();
        String mobTermId = movementDetails.getMobileTerminalGuid();
        Instant positionTime = movementDetails.getPositionTime();

        PreviousReport entity = rulesDao.getPreviousReportByAssetGuid(assetGuid);
        if (entity == null) {
            entity = new PreviousReport();
        }
        entity.setPositionTime(positionTime);
        entity.setAssetGuid(assetGuid);
        if (movementId != null)
            entity.setMovementGuid(UUID.fromString(movementId));
        if (mobTermId != null)
            entity.setMobTermGuid(UUID.fromString(mobTermId));
        entity.setUpdated(Instant.now());
        entity.setUpdatedBy("UVMS");
        rulesDao.updatePreviousReport(entity);
    }

    private boolean isLocalFlagState(String flagState) {
        try {
            if (localFlagstate == null || localFlagstate.length() > 3) {
                localFlagstate = parameterService.getStringValue(ParameterKey.LOCAL_FLAGSTATE.getKey());
            }
            return flagState.equalsIgnoreCase(localFlagstate);
        } catch (ConfigServiceException e) {
            LOG.error("Could not get local flag state", e);
            return false;
        }
    }
}
