package io.btrace.jstatsd.server.api;

/**
 * An unrecoverable exception signalizing a serious problem with the server
 * configuration.
 */
public class ConfigurationException extends RuntimeException {
    public ConfigurationException() {
    }

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
    }

    public ConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
