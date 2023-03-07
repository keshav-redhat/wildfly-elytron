package org.wildfly.security.http.client.hpi;

import org.wildfly.security.http.client.exception.ClientConfigException;

import java.net.URI;

public interface ClientConfigProvider {
    String getUsername(URI uri) throws ClientConfigException;
    String getPassword(URI uri) throws ClientConfigException;
}
