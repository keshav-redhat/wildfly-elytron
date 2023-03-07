package org.wildfly.security.http.client.hpi;

import org.kohsuke.MetaInfServices;
import org.wildfly.security.auth.client.AuthenticationContext;
import org.wildfly.security.auth.client.AuthenticationContextConfigurationClient;
import org.wildfly.security.http.client.exception.ClientConfigException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;

@MetaInfServices(value = ClientConfigProvider.class)
public class HttpMechClientConfigProviderImpl implements ClientConfigProvider{

    static final AuthenticationContextConfigurationClient AUTH_CONTEXT_CLIENT = AccessController.doPrivileged((PrivilegedAction<AuthenticationContextConfigurationClient>) AuthenticationContextConfigurationClient::new);

    @Override
    public String getUsername(URI uri) throws ClientConfigException {
        final CallbackHandler callbackHandler = AUTH_CONTEXT_CLIENT.getCallbackHandler(AUTH_CONTEXT_CLIENT.getAuthenticationConfiguration(uri, AuthenticationContext.captureCurrent()));
        NameCallback nameCallback = new NameCallback("user name");
        try {
            callbackHandler.handle(new Callback[]{nameCallback});
            return nameCallback.getName();
        } catch (IOException | UnsupportedCallbackException e) {
            throw new ClientConfigException("Name call back handle unsucessfull");
        }
    }

    @Override
    public String getPassword(URI uri) throws ClientConfigException {
        final CallbackHandler callbackHandler = AUTH_CONTEXT_CLIENT.getCallbackHandler(AUTH_CONTEXT_CLIENT.getAuthenticationConfiguration(uri, AuthenticationContext.captureCurrent()));
        PasswordCallback passwordCallback = new PasswordCallback("password", false);
        try {
            callbackHandler.handle(new Callback[]{passwordCallback});
            char[] password = passwordCallback.getPassword();
            if (password == null) {
                return null;
            }
            return new String(password);
        } catch (IOException | UnsupportedCallbackException e) {
            throw new ClientConfigException("Password callback handling unsucessfull");
        }
    }
}
