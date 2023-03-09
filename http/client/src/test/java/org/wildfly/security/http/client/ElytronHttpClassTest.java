package org.wildfly.security.http.client;

import org.junit.Assert;
import org.junit.Test;

public class ElytronHttpClassTest {

    ElytronHttpClient elytronHttpClient = new ElytronHttpClient();

    @Test
    public void testConnect() throws Exception {
        System.setProperty("wildfly.config.url", ElytronHttpClassTest.class.getClass().getResource("wildfly-config.xml").toExternalForm());
        String code = elytronHttpClient.connect("http://localhost:8080/servlet-security/SecuredServlet");
        Assert.assertEquals("200",code);
    }
}
