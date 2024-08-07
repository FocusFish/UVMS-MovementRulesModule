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
package fish.focus.uvms.movementrules.service.boundary;

import fish.focus.schema.audit.source.v1.AuditDataSourceMethod;
import fish.focus.uvms.audit.model.mapper.AuditLogModelMapper;
import fish.focus.uvms.commons.message.api.MessageConstants;
import fish.focus.uvms.movementrules.service.constants.AuditObjectTypeEnum;
import fish.focus.uvms.movementrules.service.constants.AuditOperationEnum;
import fish.focus.uvms.movementrules.service.message.producer.bean.AuditProducerBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.Queue;

@Stateless
public class AuditServiceBean {

    private static final Logger LOG = LoggerFactory.getLogger(AuditServiceBean.class);

    @Inject
    private AuditProducerBean producer;

    @Resource(mappedName = "java:/" + MessageConstants.QUEUE_MOVEMENTRULES)
    private Queue responseQueue;

    @Asynchronous
    public void sendAuditMessage(AuditObjectTypeEnum type, AuditOperationEnum operation, String affectedObject, String comment, String username) {
        try {
            String message = AuditLogModelMapper.mapToAuditLog(type.getValue(), operation.getValue(), affectedObject, comment, username);
            producer.sendModuleMessage(message, responseQueue, AuditDataSourceMethod.CREATE.value(), "");
        } catch (Exception e) {
            LOG.error("[ERROR] Error when sending message to Audit. ] {}", e.getMessage());
        }
    }
}
