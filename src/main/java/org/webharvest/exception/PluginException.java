package org.webharvest.exception;

/**
 * Runtime exception occured during plugin processors registration or creation.
 */
public class PluginException extends BaseException {
    public PluginException() {
    }

    public PluginException(String message) {
        super(message);
    }

    public PluginException(Throwable cause) {
        super(cause);
    }

    public PluginException(String message, Throwable cause) {
        super(message, cause);
    }

}