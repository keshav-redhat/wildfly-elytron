package org.wildfly.security.http.client;

import org.junit.Assert;
import org.junit.Test;

public class ElytronHttpClassTest {

    ElytronHttpClient elytronHttpClient = new ElytronHttpClient();

    @Test
    public void testConnect() throws Exception {
        String code = elytronHttpClient.connect("http://localhost:8080/servlet-security/SecuredServlet");
        Assert.assertEquals("200",code);
    }
}
