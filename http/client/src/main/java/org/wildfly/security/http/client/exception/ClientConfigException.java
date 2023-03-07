package org.wildfly.security.http.client.exception;

public class ClientConfigException extends RuntimeException {
    public ClientConfigException() {
        super();
    }

    public ClientConfigException(final String message) {
        super(message);
    }

    public ClientConfigException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ClientConfigException(final Throwable cause) {
        super(cause);
    }
}
