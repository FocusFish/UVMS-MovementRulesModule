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
package fish.focus.uvms.movementrules.service;

import java.util.ArrayList;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import fish.focus.uvms.commons.message.api.MessageConstants;
import fish.focus.uvms.movementrules.model.mapper.JAXBMarshaller;
import fish.focus.uvms.movementrules.service.message.producer.bean.RulesMessageProducerBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fish.focus.schema.exchange.module.v1.ExchangeBaseRequest;
import fish.focus.schema.exchange.service.v1.ServiceResponseType;
import fish.focus.uvms.exchange.model.mapper.ExchangeModuleResponseMapper;

@MessageDriven(mappedName = "jms/queue/UVMSExchangeEvent", activationConfig = {
        @ActivationConfigProperty(propertyName = "messagingType", propertyValue = "javax.jms.MessageListener"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "UVMSExchangeEvent"),
        @ActivationConfigProperty(propertyName = "messageSelector", propertyValue = MessageConstants.JMS_FUNCTION_PROPERTY + " NOT IN ( 'SEND_REPORT_TO_PLUGIN' ) AND JMSCorrelationID IS NULL")})


public class ExchangeModuleJmsMock implements MessageListener {

    final static Logger LOG = LoggerFactory.getLogger(ExchangeModuleJmsMock.class);
    
    @Inject
    RulesMessageProducerBean messageProducer;
    
    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        try {
            ExchangeBaseRequest  request = JAXBMarshaller.unmarshallTextMessage(textMessage, ExchangeBaseRequest .class);
            switch (request.getMethod()) {
                case LIST_SERVICES:
                    String response = ExchangeModuleResponseMapper.mapServiceListResponse(new ArrayList<ServiceResponseType>());
                    messageProducer.sendResponseMessageToSender((TextMessage) message, response);
                    break;
                default:
                    LOG.error("Message received in ExchangeMock!");
                    break;
            }
        } catch (Exception e) {
            LOG.error("UserModuleMock Error", e);
        }
    }
}
