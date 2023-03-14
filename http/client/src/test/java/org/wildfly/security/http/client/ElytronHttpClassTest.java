package org.wildfly.security.http.client;

import org.junit.Assert;
import org.junit.Test;
import org.wildfly.security.auth.client.AuthenticationContext;
import org.wildfly.security.auth.client.AuthenticationContextConfigurationClient;
import org.wildfly.security.auth.client.ElytronXmlParser;
import org.wildfly.security.auth.client.InvalidAuthenticationConfigurationException;

import java.net.URL;
import java.net.http.HttpRequest;
import java.security.PrivilegedAction;

import static java.security.AccessController.doPrivileged;

public class ElytronHttpClassTest {

    ElytronHttpClient elytronHttpClient = new ElytronHttpClient();

    public void testRequest() throws Exception{
        AuthenticationContextConfigurationClient AUTH_CONTEXT_CLIENT =
                doPrivileged((PrivilegedAction<AuthenticationContextConfigurationClient>) AuthenticationContextConfigurationClient::new);

        AuthenticationContext context = doPrivileged((PrivilegedAction<AuthenticationContext>) () -> {
            try {
                URL config = getClass().getResource("wildfly-config.xml");
                return ElytronXmlParser.parseAuthenticationClientConfiguration(config.toURI()).create();
            } catch (Throwable t) {
                throw new InvalidAuthenticationConfigurationException(t);
            }
        });
        context.run(() -> {
            try {
                HttpRequest request = elytronHttpClient.connect("http://localhost:8080/servlet-security/SecuredServlet");

            }catch (Exception e){
                throw new InvalidAuthenticationConfigurationException(e);
            }
        });
    }
}
