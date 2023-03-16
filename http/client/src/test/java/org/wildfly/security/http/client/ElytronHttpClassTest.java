package org.wildfly.security.http.client;

import org.junit.Assert;
import org.junit.Test;
import org.wildfly.security.auth.callback.AvailableRealmsCallback;
import org.wildfly.security.auth.callback.CredentialCallback;
import org.wildfly.security.auth.client.AuthenticationContext;
import org.wildfly.security.auth.client.AuthenticationContextConfigurationClient;
import org.wildfly.security.auth.client.ElytronXmlParser;
import org.wildfly.security.auth.client.InvalidAuthenticationConfigurationException;
import org.wildfly.security.credential.PasswordCredential;
import org.wildfly.security.http.HttpServerAuthenticationMechanism;
import org.wildfly.security.http.HttpServerAuthenticationMechanismFactory;
import org.wildfly.security.http.basic.BasicMechanismFactory;
import org.wildfly.security.password.Password;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.WildFlyElytronPasswordProvider;
import org.wildfly.security.password.interfaces.ClearPassword;
import org.wildfly.security.password.spec.ClearPasswordSpec;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;
import java.net.URL;
import java.net.http.HttpRequest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.function.Supplier;

import static java.security.AccessController.doPrivileged;

public class ElytronHttpClassTest {

    public static Supplier<Provider[]> ELYTRON_PASSWORD_PROVIDERS = () -> new Provider[]{
            WildFlyElytronPasswordProvider.getInstance()
    };
    protected HttpServerAuthenticationMechanismFactory basicFactory = new BasicMechanismFactory(ELYTRON_PASSWORD_PROVIDERS.get());

    ElytronHttpClient elytronHttpClient = new ElytronHttpClient();

    protected CallbackHandler getCallbackHandler(String username,String password,String realm){
        return (callbacks)->{
            Assert.assertTrue("ab",true);
            for(Callback callback : callbacks){
                if (callback instanceof AvailableRealmsCallback){
                    ((AvailableRealmsCallback) callback).setRealmNames(realm);
                } else if (callback instanceof RealmCallback) {
                    Assert.assertEquals(realm, ((RealmCallback) callback).getDefaultText());
                } else if (callback instanceof NameCallback) {
                    Assert.assertEquals(username, ((NameCallback) callback).getDefaultName());
                } else if (callback instanceof CredentialCallback) {
                    if (!ClearPassword.ALGORITHM_CLEAR.equals(((CredentialCallback) callback).getAlgorithm())) {
                        throw new UnsupportedCallbackException(callback);
                    }
                    try {
                        PasswordFactory factory = PasswordFactory.getInstance(ClearPassword.ALGORITHM_CLEAR, ELYTRON_PASSWORD_PROVIDERS);
                        Password pass = factory.generatePassword(new ClearPasswordSpec(password.toCharArray()));
                        ((CredentialCallback) callback).setCredential(new PasswordCredential(pass));
                    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        };
    }

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
                HttpRequest request = elytronHttpClient.getRequest("http://localhost:8080/servlet-security/SecuredServlet");
                Assert.assertEquals("Basic cXVpY2tzdGFydFVzZXI6cXVpY2tzdGFydFB3ZDEh",request.headers().allValues("Authorization").get(0));
            }catch (Exception e){
                throw new InvalidAuthenticationConfigurationException(e);
            }
        });
    }
}
