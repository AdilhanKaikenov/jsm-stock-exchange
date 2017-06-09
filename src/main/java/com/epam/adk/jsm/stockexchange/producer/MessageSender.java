package com.epam.adk.jsm.stockexchange.producer;

import javax.jms.Destination;
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
public class MessageSender {

    public static void main(String[] args) {

        try {
            Properties properties = new Properties();

            properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
            properties.put(Context.URL_PKG_PREFIXES, "org.jboss.naming");
            properties.put(Context.PROVIDER_URL, "http-remoting://localhost:8181/");
            properties.put(Context.SECURITY_PRINCIPAL, "admin");
            properties.put(Context.SECURITY_CREDENTIALS, "console");

            InitialContext initialContext = new InitialContext(properties);

            Destination destination = (Destination) initialContext.lookup("jms/queue/stockexchangequeue");

        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
}
