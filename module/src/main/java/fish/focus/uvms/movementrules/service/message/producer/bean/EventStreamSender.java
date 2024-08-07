package fish.focus.uvms.movementrules.service.message.producer.bean;

import fish.focus.schema.movementrules.customrule.v1.AvailabilityType;
import fish.focus.schema.movementrules.customrule.v1.SubscriptionTypeType;
import fish.focus.uvms.commons.date.JsonBConfigurator;
import fish.focus.uvms.commons.message.api.MessageConstants;
import fish.focus.uvms.commons.message.context.MappedDiagnosticContext;
import fish.focus.uvms.movementrules.service.dto.EventTicket;
import fish.focus.uvms.movementrules.service.event.TicketEvent;
import fish.focus.uvms.movementrules.service.event.TicketUpdateEvent;
import fish.focus.uvms.movementrules.service.mapper.TicketMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.TextMessage;
import javax.json.bind.Jsonb;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class EventStreamSender {

    private static final Logger LOG = LoggerFactory.getLogger(EventStreamSender.class);
    @Inject
    @JMSConnectionFactory("java:/ConnectionFactory")
    JMSContext context;
    @Resource(mappedName = "java:/" + MessageConstants.EVENT_STREAM_TOPIC)
    private Destination destination;
    private Jsonb jsonb;

    @PostConstruct
    public void init() {
        JsonBConfigurator configurator = new JsonBConfigurator();
        jsonb = configurator.getContext(null);
    }

    public void updatedTicket(@Observes(during = TransactionPhase.AFTER_SUCCESS) @TicketUpdateEvent EventTicket ticket) {
        sendEvent(ticket, "TicketUpdate");
    }

    public void createdTicket(@Observes(during = TransactionPhase.AFTER_SUCCESS) @TicketEvent EventTicket ticket) {
        sendEvent(ticket, "Ticket");
    }

    private void sendEvent(EventTicket eventTicket, String eventName) {
        if (eventTicket.getCustomRule() == null) {
            LOG.error("Rule in eventTicket {} is null", eventTicket.getTicket().getRuleName());
            return;
        }
        try {
            String outgoingJson = jsonb.toJson(TicketMapper.toTicketType(eventTicket.getTicket()));
            List<String> subscriberList = new ArrayList<>();
            String subscriberJson = null;
            if (!eventTicket.getCustomRule().getAvailability().equals(AvailabilityType.GLOBAL.value())) {
                eventTicket.getCustomRule().getRuleSubscriptionList().stream()
                        .filter(sub -> SubscriptionTypeType.TICKET.value().equals(sub.getType()))
                        .forEach(sub -> subscriberList.add(sub.getOwner()));
                subscriberJson = jsonb.toJson(subscriberList);
            }


            TextMessage message = this.context.createTextMessage(outgoingJson);
            message.setStringProperty(MessageConstants.EVENT_STREAM_EVENT, eventName);
            message.setStringProperty(MessageConstants.EVENT_STREAM_SUBSCRIBER_LIST, subscriberJson);
            MappedDiagnosticContext.addThreadMappedDiagnosticContextToMessageProperties(message);

            context.createProducer().setDeliveryMode(1).setTimeToLive(5000L).send(destination, message);

        } catch (Exception e) {
            LOG.error("Error while sending ticket event to event stream topic: ", e);
            throw new RuntimeException(e);
        }
    }

}