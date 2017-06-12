package com.epam.adk.jsm.stockexchange.ptp.producer;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

/**
 * TODO: Comment
 * <p>
 * Created on 6/9/2017.
 *
 * @author Kaikenov Adilkhan
 */
public class RemoteMessageSender {

    private static final String JBOSS_NAMING_REMOTE_INITIAL_CONTEXT_FACTORY = "org.jboss.naming.remote.client.InitialContextFactory";
    private static final String JNDI_REMOTE_CONNECTION_FACTORY = "jms/RemoteConnectionFactory";
    private static final String JNDI_QUEUE_STOCK_EXCHANGE_QUEUE = "jms/queue/stockexchangequeue";
    private static final String JBOSS_NAMING_PKG_PREFIXES = "org.jboss.naming";
    private static final String HTTP_REMOTING_URL = "http-remoting://localhost:8181/";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "console";

    private Destination remoteDestination;
    private Connection remoteQueueConnection;
    private Session remoteQueueSession;

    public static void main(String[] args) {
        new RemoteMessageSender("Buy 150 Shares of Apple");
        new RemoteMessageSender("Buy 200 Shares of Apple");
        new RemoteMessageSender("Buy 300 Shares of Apple");
    }

    public RemoteMessageSender(String message) {
        Properties properties = getProperties();

        InitialContext context;
        try {
            context = new InitialContext(properties);

            QueueConnectionFactory remoteQueueCF = (QueueConnectionFactory) context.lookup(JNDI_REMOTE_CONNECTION_FACTORY);
            remoteDestination = (Destination) context.lookup(JNDI_QUEUE_STOCK_EXCHANGE_QUEUE);

            context.close();

            remoteQueueConnection = remoteQueueCF.createConnection(USERNAME, PASSWORD);
            remoteQueueConnection.start();

            remoteQueueSession = remoteQueueConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            sendTextMessage(message);

        } catch (NamingException e) {
            System.out.println("NamingException: " + e.getMessage());
        } catch (JMSException e) {
            System.out.println("JMSException: " + e.getMessage());
        } finally {
            try {
                remoteQueueSession.close();
                remoteQueueConnection.close();
            } catch (JMSException e) {
                System.out.println("Can not close: " + e.getMessage());
            }
        }
    }

    private void sendTextMessage(String message) throws JMSException {
        TextMessage textMessage = remoteQueueSession.createTextMessage(message);
        MessageProducer queueSender = remoteQueueSession.createProducer(this.remoteDestination);
        queueSender.send(textMessage);
        queueSender.close();
        System.out.println("Successfully created order to buy 100 shares of Apple");
    }

    private Properties getProperties() {
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, JBOSS_NAMING_REMOTE_INITIAL_CONTEXT_FACTORY);
        properties.put(Context.URL_PKG_PREFIXES, JBOSS_NAMING_PKG_PREFIXES);
        properties.put(Context.PROVIDER_URL, HTTP_REMOTING_URL);
        properties.put(Context.SECURITY_PRINCIPAL, USERNAME);

        properties.put(Context.SECURITY_CREDENTIALS, PASSWORD);
        return properties;
    }
}
