package org.wildfly.security.http.client;

import org.junit.Assert;
import org.junit.Test;
import org.wildfly.security.auth.client.AuthenticationContext;
import org.wildfly.security.auth.client.AuthenticationContextConfigurationClient;
import org.wildfly.security.auth.client.ElytronXmlParser;
import org.wildfly.security.auth.client.InvalidAuthenticationConfigurationException;
import org.wildfly.security.http.HttpServerAuthenticationMechanism;
import org.wildfly.security.http.HttpServerAuthenticationMechanismFactory;
import org.wildfly.security.http.basic.BasicMechanismFactory;
import org.wildfly.security.http.impl.AbstractBaseHttpTest;
import org.wildfly.security.password.WildFlyElytronPasswordProvider;

import java.net.URL;
import java.net.http.HttpRequest;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.util.Collections;
import java.util.function.Supplier;

import static java.security.AccessController.doPrivileged;

public class ElytronHttpClassTest extends AbstractBaseHttpTest {

    public static Supplier<Provider[]> ELYTRON_PASSWORD_PROVIDERS = () -> new Provider[]{
            WildFlyElytronPasswordProvider.getInstance()
    };
    protected HttpServerAuthenticationMechanismFactory basicFactory = new BasicMechanismFactory(ELYTRON_PASSWORD_PROVIDERS.get());

    ElytronHttpClient elytronHttpClient = new ElytronHttpClient();

    @Test
    public void testRequest(){
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
                HttpServerAuthenticationMechanism mechanism = basicFactory.createAuthenticationMechanism("BASIC", Collections.emptyMap(),getCallbackHandler("quickstartUser", "test-realm", "quickstartPwd1!", null));
                HttpRequest request = elytronHttpClient.getRequest("http://localhost:8080/servlet-security/SecuredServlet");
                TestingHttpServerRequest testingHttpServerRequest = new TestingHttpServerRequest(new String[]{request.headers().allValues("Authorization").get(0)});
                mechanism.evaluateRequest(testingHttpServerRequest);
                Assert.assertEquals(Status.COMPLETE,testingHttpServerRequest.getResult());
            }catch (Exception e){
                throw new InvalidAuthenticationConfigurationException(e);
            }
        });
    }
}
