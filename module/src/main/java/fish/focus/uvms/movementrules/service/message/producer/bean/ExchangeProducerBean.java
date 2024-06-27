package fish.focus.uvms.movementrules.service.message.producer.bean;

import fish.focus.uvms.commons.message.api.MessageConstants;
import fish.focus.uvms.commons.message.impl.AbstractProducer;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;

@Stateless
public class ExchangeProducerBean extends AbstractProducer {

    @Resource(mappedName = "java:/" + MessageConstants.QUEUE_EXCHANGE_EVENT)
    private Queue destination;
    @Resource(mappedName = "java:/" + MessageConstants.QUEUE_MOVEMENTRULES)
    private Queue responseQueue;

    @Override
    public Destination getDestination() {
        return destination;
    }

    public String sendModuleMessage(String text, String function) throws JMSException {
        return this.sendMessageToSpecificQueueWithFunction(text, getDestination(), null, function, null);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public String sendSynchronousModuleMessage(String text, String function) throws JMSException {
        return this.sendMessageToSpecificQueueWithFunction(text, getDestination(), responseQueue, function, "");
    }

}
