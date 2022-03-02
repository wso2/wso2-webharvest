/*  Copyright (c) 2006-2007, Vladimir Nikic
    All rights reserved.

    Redistribution and use of this software in source and binary forms,
    with or without modification, are permitted provided that the following
    conditions are met:

    * Redistributions of source code must retain the above
      copyright notice, this list of conditions and the
      following disclaimer.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the
      following disclaimer in the documentation and/or other
      materials provided with the distribution.

    * The name of Web-Harvest may not be used to endorse or promote
      products derived from this software without specific prior
      written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.

    You can contact Vladimir Nikic by sending e-mail to
    nikic_vladimir@yahoo.com. Please include the word "Web-Harvest" in the
    subject line.
*/
package org.webharvest.gui;

import org.webharvest.definition.DefinitionResolver;
import org.webharvest.exception.PluginException;
import org.webharvest.utils.CommonUtil;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;

/**
 * @author: Vladimir Nikic
 * Date: Apr 27, 2007
 */
public class Settings implements Serializable {

    private static final String CONFIG_FILE_PATH = System.getProperty("java.io.tmpdir") + "/webharvest.config";
    private static final int MAX_RECENT_FILES = 20;

    private String workingPath = System.getProperty("java.io.tmpdir");
    private String fileCharset = "UTF-8";
    private boolean isProxyEnabled;
    private String proxyServer;
    private int proxyPort = -1;
    private boolean isProxyAuthEnabled;
    private String proxyUserename;
    private String proxyPassword;
    private boolean isNtlmAuthEnabled;
    private String ntlmHost;
    private String ntlmDomain;

    private boolean isShowHierarchyByDefault = true;
    private boolean isShowLogByDefault = true;
    private boolean isShowLineNumbersByDefault = true;

    // specify if processors are located in source while configuration is running
    private boolean isDynamicConfigLocate = true;

    // specify if info is displayed on execution finish
    private boolean isShowFinishDialog = true;

    // array of plugins
    private String plugins[] = {};

    // list of recently open files
    private List recentFiles = new LinkedList();

    public Settings() {
        try {
            readFromFile();
        } catch (IOException e) {
            e.printStackTrace();
            GuiUtils.showErrorMessage("Error while reading programs settings: " + e.getMessage());
        }
    }

    public boolean isProxyAuthEnabled() {
        return isProxyAuthEnabled;
    }

    public void setProxyAuthEnabled(boolean proxyAuthEnabled) {
        isProxyAuthEnabled = proxyAuthEnabled;
    }

    public boolean isProxyEnabled() {
        return isProxyEnabled;
    }

    public void setProxyEnabled(boolean proxyEnabled) {
        isProxyEnabled = proxyEnabled;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyServer() {
        return proxyServer;
    }

    public void setProxyServer(String proxyServer) {
        this.proxyServer = proxyServer;
    }

    public String getProxyUserename() {
        return proxyUserename;
    }

    public void setProxyUserename(String proxyUserename) {
        this.proxyUserename = proxyUserename;
    }

    public boolean isNtlmAuthEnabled() {
        return isNtlmAuthEnabled;
    }

    public void setNtlmAuthEnabled(boolean ntlmAuthEnabled) {
        isNtlmAuthEnabled = ntlmAuthEnabled;
    }

    public String getNtlmDomain() {
        return ntlmDomain;
    }

    public void setNtlmDomain(String ntlmDomain) {
        this.ntlmDomain = ntlmDomain;
    }

    public String getNtlmHost() {
        return ntlmHost;
    }

    public void setNtlmHost(String ntlmHost) {
        this.ntlmHost = ntlmHost;
    }

    public String getWorkingPath() {
        return workingPath;
    }

    public void setWorkingPath(String workingPath) {
        this.workingPath = workingPath;
    }

    public String getFileCharset() {
        return fileCharset;
    }

    public void setFileCharset(String fileCharset) {
        this.fileCharset = fileCharset;
    }

    public boolean isDynamicConfigLocate() {
        return this.isDynamicConfigLocate;
    }

    public void setDynamicConfigLocate(boolean dynamicConfigLocate) {
        isDynamicConfigLocate = dynamicConfigLocate;
    }

    public boolean isShowFinishDialog() {
        return isShowFinishDialog;
    }

    public void setShowFinishDialog(boolean showFinishDialog) {
        isShowFinishDialog = showFinishDialog;
    }

    public boolean isShowHierarchyByDefault() {
        return isShowHierarchyByDefault;
    }

    public void setShowHierarchyByDefault(boolean showHierarchyByDefault) {
        isShowHierarchyByDefault = showHierarchyByDefault;
    }

    public boolean isShowLogByDefault() {
        return isShowLogByDefault;
    }

    public void setShowLogByDefault(boolean showLogByDefault) {
        isShowLogByDefault = showLogByDefault;
    }

    public boolean isShowLineNumbersByDefault() {
        return isShowLineNumbersByDefault;
    }

    public void setShowLineNumbersByDefault(boolean showLineNumbersByDefault) {
        isShowLineNumbersByDefault = showLineNumbersByDefault;
    }

    public String[] getPlugins() {
        return plugins;
    }

    public void setPlugins(String[] plugins) {
        this.plugins = plugins == null ? new String[] {} : plugins;
    }

    public List getRecentFiles() {
        return recentFiles;
    }

    public void addRecentFile(String filePath) {
        int index = CommonUtil.findValueInCollection(recentFiles, filePath);
        if (index >= 0) {
            recentFiles.remove(index);
        }
        recentFiles.add(0, filePath);

        int recentFilesCount = recentFiles.size();
        if (recentFilesCount > MAX_RECENT_FILES) {
            recentFiles.remove(recentFilesCount - 1);
        }
    }

    private void writeString(ObjectOutputStream out, String s) throws IOException {
        if (s != null) {
            out.writeInt(s.getBytes().length);
            out.writeBytes(s);
        } else {
            out.writeInt(0);
        }
    }

    private String readString(ObjectInputStream in, String defaultValue) throws IOException {
        try {
            byte[] bytes = new byte[in.readInt()];
            in.read(bytes);
            return new String(bytes);
        } catch (IOException e) {
            return defaultValue;
        }
    }

    private boolean readBoolean(ObjectInputStream in, boolean defaultValue) throws IOException {
        try {
            return in.readBoolean();
        } catch (IOException e) {
            return defaultValue;
        }
    }

    private int readInt(ObjectInputStream in, int defaultValue) throws IOException {
        try {
            return in.readInt();
        } catch (IOException e) {
            return defaultValue;
        }
    }

    /**
     * Serialization write.
     * @param out
     * @throws IOException
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        writeString(out, workingPath);

        out.writeBoolean(isProxyEnabled);
        writeString(out, proxyServer);
        out.writeInt(proxyPort);
        out.writeBoolean(isProxyAuthEnabled);
        writeString(out, proxyUserename);
        writeString(out, proxyPassword);
        out.writeBoolean(isNtlmAuthEnabled);
        writeString(out, ntlmHost);
        writeString(out, ntlmDomain);

        out.writeBoolean(isShowHierarchyByDefault);
        out.writeBoolean(isShowLogByDefault);
        out.writeBoolean(isShowLineNumbersByDefault);
        out.writeBoolean(isDynamicConfigLocate);

        writeString(out, fileCharset);

        out.writeBoolean(isShowFinishDialog);

        out.writeInt(plugins.length);
        for (int i = 0; i < plugins.length; i++) {
            writeString(out, plugins[i]);
        }

        out.writeInt(recentFiles.size());
        Iterator iterator = recentFiles.iterator();
        while (iterator.hasNext()) {
            writeString(out, (String) iterator.next());
        }
    }

    /**
     * Serialization read.
     * @param in
     * @throws IOException
     */
    private void readObject(ObjectInputStream in) throws IOException {
        workingPath = readString(in, workingPath);

        isProxyEnabled = readBoolean(in, isProxyEnabled);
        proxyServer = readString(in, proxyServer);
        proxyPort = readInt(in, proxyPort);
        isProxyAuthEnabled = readBoolean(in, isProxyAuthEnabled);
        proxyUserename = readString(in, proxyUserename);
        proxyPassword = readString(in, proxyPassword);
        isNtlmAuthEnabled = readBoolean(in, isNtlmAuthEnabled);
        ntlmHost = readString(in, ntlmHost);
        ntlmDomain = readString(in, ntlmDomain);

        isShowHierarchyByDefault = readBoolean(in, isShowHierarchyByDefault);
        isShowLogByDefault = readBoolean(in, isShowLogByDefault);
        isShowLineNumbersByDefault = readBoolean(in, isShowLineNumbersByDefault);
        isDynamicConfigLocate = readBoolean(in, isDynamicConfigLocate);

        fileCharset = readString(in, fileCharset);

        isShowFinishDialog = readBoolean(in, isShowFinishDialog);

        int pluginsCount = readInt(in, 0);
        plugins = new String[pluginsCount];
        for (int i = 0; i < pluginsCount; i++) {
            plugins[i] = readString(in, "");
            try {
                DefinitionResolver.registerPlugin(plugins[i]);
            } catch (PluginException e) {
                // do nothing - try silently to register plugins
            }
        }

        int recentFilesCount = readInt(in, 0);
        recentFiles.clear();
        for (int i = 0; i < recentFilesCount; i++) {
            recentFiles.add(readString(in, ""));
        }
    }

    private void readFromFile() throws IOException {
        File configFile = new File(CONFIG_FILE_PATH);
        if ( configFile.exists() ) {
            FileInputStream fis = new FileInputStream(configFile);
	        ObjectInputStream ois = new ObjectInputStream(fis);
            readObject(ois);
        }
    }

    public void writeToFile() throws IOException {
        File configFile = new File(CONFIG_FILE_PATH);
        FileOutputStream fos = new FileOutputStream(configFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        writeObject(oos);
        oos.flush();
        fos.flush();
        oos.close();
        fos.close();
    }

    public void writeSilentlyToFile() {
        try {
            writeToFile();
        } catch (IOException e) {
            // ignore
        }
    }

}