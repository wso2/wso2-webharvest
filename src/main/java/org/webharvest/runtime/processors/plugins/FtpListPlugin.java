package org.webharvest.runtime.processors.plugins;

import org.apache.commons.net.ftp.*;
import org.webharvest.runtime.*;
import org.webharvest.runtime.processors.*;
import org.webharvest.runtime.variables.*;
import org.webharvest.utils.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.nio.charset.*;

/**
 * Ftp List plugin - can be used only inside ftp plugin for listing file in working remote directory.
 */
public class FtpListPlugin extends WebHarvestPlugin {

    public String getName() {
        return "ftp-list";
    }

    public Variable executePlugin(Scraper scraper, ScraperContext context) {
        FtpPlugin ftpPlugin = (FtpPlugin) scraper.getRunningProcessorOfType(FtpPlugin.class);
        if (ftpPlugin != null) {
            FTPClient ftpClient = ftpPlugin.getFtpClient();

            String path = CommonUtil.nvl( evaluateAttribute("path", scraper), "" );
            boolean listFiles = evaluateAttributeAsBoolean("listfiles", true, scraper);
            boolean listDirs = evaluateAttributeAsBoolean("listdirs", true, scraper);
            boolean listLinks = evaluateAttributeAsBoolean("listlinks", true, scraper);
            String listFilter = CommonUtil.nvl( evaluateAttribute("listfilter", scraper), "" );

            Pattern pattern = null;
            if ( !CommonUtil.isEmptyString(listFilter) ) {
                StringBuffer buffer = new StringBuffer();
                for (int i = 0; i < listFilter.length(); i++) {
                    char ch = listFilter.charAt(i);
                    switch (ch) {
                        case '.' : buffer.append("\\."); break;
                        case '*' : buffer.append(".*"); break;
                        case '?' : buffer.append("."); break;
                        default : buffer.append(ch); break;
                    }
                }
                try {
                    pattern = Pattern.compile( buffer.toString() );
                } catch (Exception e) {
                    pattern = Pattern.compile("");
                }
            }

            setProperty("Path", path);
            setProperty("List Files", listFiles);
            setProperty("List Directories", listDirs);
            setProperty("List Symbolic Links", listLinks);
            setProperty("List Filter", listFilter);

            try {
                FTPFile[] files = ftpClient.listFiles(path);
                if (files != null) {
                    List<String> filenameList = new ArrayList<String>();
                    for (FTPFile ftpFile: files) {
                        if ( (listFiles && ftpFile.isFile()) || (listDirs && ftpFile.isDirectory()) || (listLinks && ftpFile.isSymbolicLink()) ) {
                            String name = ftpFile.getName();
                            if ( pattern == null || pattern.matcher(name).matches() ) {
                                filenameList.add(name);
                            }
                        }
                    }

                    return new ListVariable(filenameList);
                }
            } catch (IOException e) {
                throw new FtpPluginException(e);
            }
        } else {
            throw new FtpPluginException("Cannot use ftp list plugin out of ftp plugin context!");
        }
        
        return new EmptyVariable();
    }

    public String[] getValidAttributes() {
        return new String[] {"path", "listfiles", "listdirs", "listlinks", "listfilter"};
    }

    public String[] getRequiredAttributes() {
        return new String[] {};
    }

    public boolean hasBody() {
        return false;
    }

    public String[] getAttributeValueSuggestions(String attributeName) {
        if ( "listfiles".equalsIgnoreCase(attributeName) ||
             "listdirs".equalsIgnoreCase(attributeName) ||
             "listlinks".equalsIgnoreCase(attributeName) ) {
            return new String[] {"true", "false"};
        }
        return null;
    }

}