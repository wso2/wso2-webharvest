package org.webharvest.runtime.processors.plugins;

import org.webharvest.exception.*;

/**
 * Runtime exception for MailPlugin
 */
public class MailPluginException extends BaseException {

    public MailPluginException() {
    }

    public MailPluginException(String message) {
        super(message);
    }

    public MailPluginException(Throwable cause) {
        super(cause);
    }

    public MailPluginException(String message, Throwable cause) {
        super(message, cause);
    }

}