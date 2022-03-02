package org.webharvest.runtime.processors.plugins;

import org.webharvest.exception.*;

/**
 * Runtime exception for ZipPlugin
 */
public class ZipPluginException extends BaseException {

    public ZipPluginException() {
    }

    public ZipPluginException(String message) {
        super(message);
    }

    public ZipPluginException(Throwable cause) {
        super(cause);
    }

    public ZipPluginException(String message, Throwable cause) {
        super(message, cause);
    }

}