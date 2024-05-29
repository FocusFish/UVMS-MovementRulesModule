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
package fish.focus.uvms.movementrules.service.message;

import fish.focus.uvms.commons.message.api.MessageConstants;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.api.jms.JMSFactoryType;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

import static fish.focus.uvms.commons.message.api.MessageConstants.EVENT_STREAM_EVENT;
import static fish.focus.uvms.commons.message.api.MessageConstants.EVENT_STREAM_TOPIC;
import static javax.jms.DeliveryMode.PERSISTENT;

public class JMSHelper {

    private static final long TIMEOUT = 5000;

    @Resource(lookup = "java:/" + EVENT_STREAM_TOPIC)
    private Destination eventStreamDestination;

    @Inject
    @JMSConnectionFactory("java:/ConnectionFactory")
    JMSContext context;

    public void sendMessageOnEventStream(String message, String eventName) {
        context.createProducer()
                .setProperty(EVENT_STREAM_EVENT, eventName)
                .setDeliveryMode(PERSISTENT)
                .send(eventStreamDestination, message);
    }

    public String sendMessageToRules(String text, String requestType, String resQueue) throws Exception {
        Connection connection = getConnectionFactory().createConnection("test", "test");
        try {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue responseQueue = session.createQueue(resQueue);
            Queue movementRulesEventQueue = session.createQueue(MessageConstants.QUEUE_MOVEMENTRULES_EVENT_NAME);

            TextMessage message = session.createTextMessage();
            message.setJMSReplyTo(responseQueue);
            message.setText(text);
            message.setStringProperty(MessageConstants.JMS_FUNCTION_PROPERTY, requestType);

            session.createProducer(movementRulesEventQueue).send(message);

            return message.getJMSMessageID();
        } finally {
            connection.close();
        }
    }

    public Message listenOnQueue(String queue) throws Exception {
        Connection connection = getConnectionFactory().createConnection("test", "test");
        try {
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue responseQueue = session.createQueue(queue);

            return session.createConsumer(responseQueue).receive(TIMEOUT);
        } finally {
            connection.close();
        }
    }

    public void clearQueue(String queue) throws Exception {
        Connection connection = getConnectionFactory().createConnection("test", "test");
        MessageConsumer consumer;
        try {
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue responseQueue = session.createQueue(queue);
            consumer = session.createConsumer(responseQueue);

            while (consumer.receive(10L) != null) ;
        } finally {
            connection.close();
        }
    }

    private ConnectionFactory getConnectionFactory() {
        Map<String, Object> params = new HashMap<>();
        params.put("host", "localhost");
        params.put("port", 5445);
        TransportConfiguration transportConfiguration = new TransportConfiguration(NettyConnectorFactory.class.getName(), params);
        return ActiveMQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, transportConfiguration);
    }
}
