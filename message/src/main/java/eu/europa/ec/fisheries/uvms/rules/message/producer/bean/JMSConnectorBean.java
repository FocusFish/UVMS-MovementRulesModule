package eu.europa.ec.fisheries.uvms.rules.message.producer.bean;

import eu.europa.ec.fisheries.uvms.rules.message.constants.MessageConstants;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.jms.*;

@Startup
@Singleton
@DependsOn("RulesMessageProducerBean")
public class JMSConnectorBean {
    final static org.slf4j.Logger LOG = LoggerFactory.getLogger(JMSConnectorBean.class);

    @Resource(lookup = MessageConstants.CONNECTION_FACTORY)
    private ConnectionFactory connectionFactory;

    private PooledConnectionFactory pooledConnectionFactory;
    private Connection connection;

    @PostConstruct
    private void connectToQueue() {
        LOG.debug("Open connection to JMS broker");
        try {
//            pooledConnectionFactory = new PooledConnectionFactory();
//            pooledConnectionFactory.setConnectionFactory(connectionFactory);
//            connection = pooledConnectionFactory.createConnection();
            connection = connectionFactory.createConnection();
            connection.start();
        } catch (JMSException ex) {
            LOG.error("Error when open connection to JMS broker");
        }
    }

    public Session getNewSession() throws JMSException {
        if (connection == null) {
            connectToQueue();
        }
        Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
        return session;
    }

    public TextMessage createTextMessage(Session session, String message) throws JMSException {
        return session.createTextMessage(message);
    }

    @PreDestroy
    private void closeConnection() {
        LOG.debug("Close connection to JMS broker");
        try {
            if (connection != null) {
                connection.stop();
                connection.close();
            }
        } catch (JMSException e) {
            LOG.warn("[ Error when stopping or closing JMS connection. ] {}", e.getMessage());
        }
    }

}
