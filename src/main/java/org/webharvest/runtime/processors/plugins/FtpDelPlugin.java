package org.webharvest.runtime.processors.plugins;

import org.apache.commons.net.ftp.*;
import org.webharvest.runtime.*;
import org.webharvest.runtime.processors.*;
import org.webharvest.runtime.variables.*;
import org.webharvest.utils.*;

import java.io.*;

/**
 * Ftp Del plugin - can be used only inside ftp plugin for deleting file on remote directory.
 */
public class FtpDelPlugin extends WebHarvestPlugin {

    public String getName() {
        return "ftp-del";
    }

    public Variable executePlugin(Scraper scraper, ScraperContext context) {
        FtpPlugin ftpPlugin = (FtpPlugin) scraper.getRunningProcessorOfType(FtpPlugin.class);
        if (ftpPlugin != null) {
            FTPClient ftpClient = ftpPlugin.getFtpClient();

            String path = CommonUtil.nvl( evaluateAttribute("path", scraper), "" );

            setProperty("Path", path);

            try {
                boolean succ = ftpClient.deleteFile(path);
                if (!succ) {
                    throw new FtpPluginException("Cannot delete file \"" + path + "\" on FTP server!");
                }
            } catch (IOException e) {
                throw new FtpPluginException(e);
            }
        } else {
            throw new FtpPluginException("Cannot use ftp del plugin out of ftp plugin context!");
        }

        return new EmptyVariable();
    }

    public String[] getValidAttributes() {
        return new String[] {"path"};
    }

    public String[] getRequiredAttributes() {
        return new String[] {"path"};
    }

    public boolean hasBody() {
        return false;
    }
    
}