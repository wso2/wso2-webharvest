package org.webharvest.runtime.processors.plugins;

import org.apache.commons.net.ftp.*;
import org.webharvest.runtime.*;
import org.webharvest.runtime.processors.*;
import org.webharvest.runtime.variables.*;
import org.webharvest.utils.*;

import java.io.*;
import java.util.*;
import java.nio.charset.*;

/**
 * Ftp Put plugin - can be used only inside ftp plugin for storing file to remote directory.
 */
public class FtpPutPlugin extends WebHarvestPlugin {

    public String getName() {
        return "ftp-put";
    }

    public Variable executePlugin(Scraper scraper, ScraperContext context) {
        FtpPlugin ftpPlugin = (FtpPlugin) scraper.getRunningProcessorOfType(FtpPlugin.class);
        if (ftpPlugin != null) {
            FTPClient ftpClient = ftpPlugin.getFtpClient();

            String path = CommonUtil.nvl( evaluateAttribute("path", scraper), "" );
            String charset = CommonUtil.nvl( evaluateAttribute("charset", scraper), "" );
            if (CommonUtil.isEmptyString(charset)) {
                charset = scraper.getConfiguration().getCharset();
            }

            setProperty("Path", path);
            setProperty("Charset", charset);

            Variable body = executeBody(scraper, scraper.getContext());

            try {
                ByteArrayInputStream stream = new ByteArrayInputStream(body.toBinary(charset));
                boolean succ = ftpClient.storeFile(path, stream);
                stream.close();
                if (!succ) {
                    throw new FtpPluginException("Cannot store file \"" + path + "\" to FTP server!");
                }
            } catch (IOException e) {
                throw new FtpPluginException(e);
            }
        } else {
            throw new FtpPluginException("Cannot use ftp put plugin out of ftp plugin context!");
        }

        return new EmptyVariable();
    }

    public String[] getValidAttributes() {
        return new String[] {"path", "charset"};
    }

    public String[] getRequiredAttributes() {
        return new String[] {"path"};
    }

    public String[] getAttributeValueSuggestions(String attributeName) {
        if ("charset".equalsIgnoreCase(attributeName)) {
            Set<String> charsetKeys = Charset.availableCharsets().keySet();
            return new ArrayList<String>(charsetKeys).toArray(new String[charsetKeys.size()]);
        }
        return null;
    }

}