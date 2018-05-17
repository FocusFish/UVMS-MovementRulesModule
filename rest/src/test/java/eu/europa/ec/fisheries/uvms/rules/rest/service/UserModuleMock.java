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
package eu.europa.ec.fisheries.uvms.rules.rest.service;

import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import eu.europa.ec.fisheries.uvms.rules.message.producer.RulesMessageProducer;
import eu.europa.ec.fisheries.uvms.user.model.mapper.UserModuleResponseMapper;
import eu.europa.ec.fisheries.wsdl.user.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@MessageDriven(mappedName = "jms/queue/UVMSUserEvent", activationConfig = {
        @ActivationConfigProperty(propertyName = "messagingType", propertyValue = "javax.jms.MessageListener"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "UVMSUserEvent")})
public class UserModuleMock implements MessageListener {

    final static Logger LOG = LoggerFactory.getLogger(UserModuleMock.class);
    
    @Inject
    RulesMessageProducer messageProducer;
    
    @Override
    public void onMessage(Message message) {
        try {
        
        UserContext userContext = getRulesUserContext();
        String responseString;
            responseString = UserModuleResponseMapper.mapToGetUserContextResponse(userContext);

        messageProducer.sendModuleResponseMessage((TextMessage) message, responseString);

        } catch (Exception e) {
            LOG.error("UserModuleMock Error", e);
        }
    }
    
    private UserContext getRulesUserContext() {
        UserContext userContext = new UserContext();
        userContext.setContextSet(new ContextSet());
        Context context = new Context();
        context.setRole(new Role());
        Feature viewAlarmsFeature = new Feature();
        viewAlarmsFeature.setName(UnionVMSFeature.viewAlarmRules.name());
        context.getRole().getFeature().add(viewAlarmsFeature);
        Feature manageAlarmsFeature = new Feature();
        manageAlarmsFeature.setName(UnionVMSFeature.manageAlarmRules.name());
        context.getRole().getFeature().add(manageAlarmsFeature);
        Feature viewAlarmsHoldingTableFeature = new Feature();
        viewAlarmsHoldingTableFeature.setName(UnionVMSFeature.viewAlarmsHoldingTable.name());
        context.getRole().getFeature().add(viewAlarmsHoldingTableFeature);
        Feature manageAlarmsHoldingTableFeature = new Feature();
        manageAlarmsHoldingTableFeature.setName(UnionVMSFeature.manageAlarmsHoldingTable.name());
        context.getRole().getFeature().add(manageAlarmsHoldingTableFeature);
        userContext.getContextSet().getContexts().add(context);
        return userContext;
    }
}
