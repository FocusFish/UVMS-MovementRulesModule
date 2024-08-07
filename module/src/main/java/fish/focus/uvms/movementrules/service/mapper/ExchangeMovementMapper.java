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
package fish.focus.uvms.movementrules.service.mapper;

import fish.focus.schema.exchange.movement.asset.v1.AssetId;
import fish.focus.schema.exchange.movement.asset.v1.AssetIdType;
import fish.focus.schema.exchange.movement.v1.*;
import fish.focus.uvms.movementrules.model.dto.MovementDetails;

import java.util.Date;

public class ExchangeMovementMapper {

    private ExchangeMovementMapper() {
    }

    ;

    public static MovementType mapToExchangeMovementType(MovementDetails movementDetails) {
        MovementType movement = new MovementType();
        movement.setGuid(movementDetails.getMovementGuid());
        movement.setConnectId(movementDetails.getConnectId());
        movement.setReportedCourse(movementDetails.getReportedCourse());
        movement.setCalculatedCourse(movementDetails.getCalculatedCourse());
        movement.setReportedSpeed(movementDetails.getReportedSpeed());
        movement.setCalculatedSpeed(movementDetails.getCalculatedSpeed());
        MovementMetaData metadata = new MovementMetaData();
        metadata.setClosestCountryCoast(movementDetails.getClosestCountryCode());
        metadata.setDistanceToCountryCoast(movementDetails.getClosestCountryDistance());
        metadata.setClosestPort(movementDetails.getClosestPortCode());
        metadata.setDistanceToClosestPort(movementDetails.getClosestPortDistance());
        movement.setMetaData(metadata);
        movement.setWkt(movementDetails.getWkt());
        MovementActivityType activity = new MovementActivityType();
        if (movementDetails.getActivityMessageType() != null) {
            activity.setMessageType(MovementActivityTypeType.fromValue(movementDetails.getActivityMessageType()));
        }
        activity.setMessageId(movementDetails.getActivityMessageId());
        activity.setCallback(movementDetails.getActivityCallback());
        movement.setActivity(activity);
        movement.setAssetId(mapAssetId(movementDetails));
        movement.setAssetName(movementDetails.getAssetName());
        movement.setFlagState(movementDetails.getFlagState());
        movement.setExternalMarking(movementDetails.getExternalMarking());
        movement.setIrcs(movementDetails.getIrcs());
        movement.setMmsi(movementDetails.getMmsi());
        if (movementDetails.getComChannelType() != null) {
            movement.setComChannelType(MovementComChannelType.valueOf(movementDetails.getComChannelType()));
        }
        if (movementDetails.getMovementType() != null) {
            movement.setMovementType(MovementTypeType.valueOf(movementDetails.getMovementType()));
        }
        MovementPoint position = new MovementPoint();
        position.setLongitude(movementDetails.getLongitude());
        position.setLatitude(movementDetails.getLatitude());
        position.setAltitude(movementDetails.getAltitude());
        movement.setPosition(position);
        movement.setPositionTime(Date.from(movementDetails.getPositionTime()));
        if (movementDetails.getSource() != null) {
            movement.setSource(MovementSourceType.valueOf(movementDetails.getSource()));
        }
        movement.setStatus(movementDetails.getStatusCode());
        movement.setSourceSatelliteId(movementDetails.getSourceSatelliteId());

        return movement;
    }

    private static AssetId mapAssetId(MovementDetails movementDetails) {
        AssetId assetId = new AssetId();
        if (movementDetails.getCfr() != null) {
            fish.focus.schema.exchange.movement.asset.v1.AssetIdList idList = new fish.focus.schema.exchange.movement.asset.v1.AssetIdList();
            idList.setIdType(AssetIdType.CFR);
            idList.setValue(movementDetails.getCfr());
            assetId.getAssetIdList().add(idList);
        }
        if (movementDetails.getIrcs() != null) {
            fish.focus.schema.exchange.movement.asset.v1.AssetIdList idList = new fish.focus.schema.exchange.movement.asset.v1.AssetIdList();
            idList.setIdType(AssetIdType.IRCS);
            idList.setValue(movementDetails.getIrcs());
            assetId.getAssetIdList().add(idList);
        }
        if (movementDetails.getMmsi() != null) {
            fish.focus.schema.exchange.movement.asset.v1.AssetIdList idList = new fish.focus.schema.exchange.movement.asset.v1.AssetIdList();
            idList.setIdType(AssetIdType.MMSI);
            idList.setValue(movementDetails.getMmsi());
            assetId.getAssetIdList().add(idList);
        }
        if (movementDetails.getImo() != null) {
            fish.focus.schema.exchange.movement.asset.v1.AssetIdList idList = new fish.focus.schema.exchange.movement.asset.v1.AssetIdList();
            idList.setIdType(AssetIdType.IMO);
            idList.setValue(movementDetails.getImo());
            assetId.getAssetIdList().add(idList);
        }
        return assetId;
    }
}