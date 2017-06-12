package com.epam.adk.jsm.stockexchange.ptp.consumer;

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
public class RemoteMessageReceiver implements MessageListener {

    private static final String JBOSS_NAMING_REMOTE_INITIAL_CONTEXT_FACTORY = "org.jboss.naming.remote.client.InitialContextFactory";
    private static final String JNDI_REMOTE_CONNECTION_FACTORY = "jms/RemoteConnectionFactory";
    private static final String JNDI_QUEUE_STOCK_EXCHANGE_QUEUE = "jms/queue/stockexchangequeue";
    private static final String JBOSS_NAMING_PKG_PREFIXES = "org.jboss.naming";
    private static final String HTTP_REMOTING_URL = "http-remoting://localhost:8181/";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "console";

    private Connection remoteQueueConnection;
    private Session remoteQueueSession;
    private MessageConsumer messageConsumer;

    public static void main(String[] args) {
        new RemoteMessageReceiver();
    }

    private RemoteMessageReceiver() {
        Properties properties = getProperties();
        try {
            InitialContext context = new InitialContext(properties);

            QueueConnectionFactory remoteQueueCF = (QueueConnectionFactory) context.lookup(JNDI_REMOTE_CONNECTION_FACTORY);
            Destination remoteDestination = (Destination) context.lookup(JNDI_QUEUE_STOCK_EXCHANGE_QUEUE);

            context.close();

            remoteQueueConnection = remoteQueueCF.createConnection(USERNAME, PASSWORD);
            remoteQueueConnection.start();

            remoteQueueSession = remoteQueueConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            messageConsumer = remoteQueueSession.createConsumer(remoteDestination);
            messageConsumer.setMessageListener(this);

            System.out.println("Listening to the stockexchangequeue...");
            // Wait for Text messages
            Thread.sleep(200000);

        } catch (InterruptedException e) {
            System.out.println("InterruptedException: " + e.getMessage());
        } catch (NamingException e) {
            System.out.println("NamingException: " + e.getMessage());
        } catch (JMSException e) {
            System.out.println("JMSException: " + e.getMessage());
        } finally {
            try {
                messageConsumer.close();
                remoteQueueSession.close();
                remoteQueueConnection.close();
            } catch (JMSException e) {
                System.out.println("Can not close: " + e.getMessage());
            }
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            String msgText = ((TextMessage) message).getText();
            System.out.println("Got from the Remote Queue: " + msgText);
        } catch (JMSException e) {
            e.printStackTrace();
        }
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
