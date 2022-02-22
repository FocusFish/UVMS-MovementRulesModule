package fish.focus.uvms.movementrules.service.message.producer.bean;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fish.focus.uvms.commons.message.api.MessageConstants;
import fish.focus.uvms.commons.message.impl.AbstractProducer;
import fish.focus.uvms.config.exception.ConfigMessageException;
import fish.focus.uvms.config.message.ConfigMessageProducer;

@Stateless
@LocalBean
public class ConfigMessageProducerBean extends AbstractProducer implements ConfigMessageProducer {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigMessageProducerBean.class);

    @Resource(mappedName =  "java:/" + MessageConstants.QUEUE_CONFIG)
    private Queue destination;

    @Resource(mappedName = "java:/" + MessageConstants.QUEUE_MOVEMENTRULES)
    private Queue responseQueue;

    @Override
    public Destination getDestination() {
        return destination;
    }
    
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public String sendConfigMessage(String text) throws ConfigMessageException {
        try {
            return sendMessageToSpecificQueueWithFunction(text, getDestination(), responseQueue, null , null);
        } catch (JMSException e) {
            LOG.error("[ Error when sending config message. ] {}", e.getMessage());
            throw new ConfigMessageException("[ Error when sending config message. ]");
        }
    }
}
