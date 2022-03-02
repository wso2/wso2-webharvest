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

import org.webharvest.definition.*;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Vladimir Nikic
 * Date: Apr 19, 2007
 */
public class TreeNodeInfo {

    private static Icon DEFAULT_ICON = ResourceManager.getIcon("resources/icons/default.gif");
    private static Map icons = new HashMap();

    static {
        icons.put(LoopDef.class, ResourceManager.getIcon("resources/icons/loop.gif"));
        icons.put(WhileDef.class, ResourceManager.getIcon("resources/icons/loop.gif"));
        icons.put(VarDefDef.class, ResourceManager.getIcon("resources/icons/vardef.gif"));
        icons.put(VarDef.class, ResourceManager.getIcon("resources/icons/var.gif"));
        icons.put(ConstantDef.class, ResourceManager.getIcon("resources/icons/const.gif"));
        icons.put(TextDef.class, ResourceManager.getIcon("resources/icons/text.gif"));
        icons.put(HttpDef.class, ResourceManager.getIcon("resources/icons/http.gif"));
        icons.put(XQueryDef.class, ResourceManager.getIcon("resources/icons/xquery.gif"));
        icons.put(XsltDef.class, ResourceManager.getIcon("resources/icons/xslt.gif"));
        icons.put(XPathDef.class, ResourceManager.getIcon("resources/icons/xpath.gif"));
        icons.put(RegexpDef.class, ResourceManager.getIcon("resources/icons/regexp.gif"));
        icons.put(TemplateDef.class, ResourceManager.getIcon("resources/icons/template.gif"));
        icons.put(FileDef.class, ResourceManager.getIcon("resources/icons/file.gif"));
        icons.put(HtmlToXmlDef.class, ResourceManager.getIcon("resources/icons/htmltoxml.gif"));
        icons.put(EmptyDef.class, ResourceManager.getIcon("resources/icons/empty.gif"));
        icons.put(IncludeDef.class, ResourceManager.getIcon("resources/icons/include.gif"));
        icons.put(FunctionDef.class, ResourceManager.getIcon("resources/icons/function.gif"));
        icons.put(CallDef.class, ResourceManager.getIcon("resources/icons/call.gif"));
        icons.put(CaseDef.class, ResourceManager.getIcon("resources/icons/case.gif"));
        icons.put(TryDef.class, ResourceManager.getIcon("resources/icons/try.gif"));
        icons.put("database", ResourceManager.getIcon("resources/icons/database.gif"));
        icons.put("xml-to-json", ResourceManager.getIcon("resources/icons/xmltojson.gif"));
        icons.put("json-to-xml", ResourceManager.getIcon("resources/icons/jsontoxml.gif"));
        icons.put("mail", ResourceManager.getIcon("resources/icons/mail.gif"));
        icons.put("mail-attach", ResourceManager.getIcon("resources/icons/mailattach.gif"));
        icons.put("zip", ResourceManager.getIcon("resources/icons/zip.gif"));
        icons.put("zip-entry", ResourceManager.getIcon("resources/icons/zipentry.gif"));
        icons.put("ftp", ResourceManager.getIcon("resources/icons/ftp.gif"));
        icons.put("ftp-del", ResourceManager.getIcon("resources/icons/ftp.gif"));
        icons.put("ftp-get", ResourceManager.getIcon("resources/icons/ftp.gif"));
        icons.put("ftp-list", ResourceManager.getIcon("resources/icons/ftp.gif"));
        icons.put("ftp-put", ResourceManager.getIcon("resources/icons/ftp.gif"));
        icons.put("ftp-mkdir", ResourceManager.getIcon("resources/icons/ftp.gif"));
        icons.put("ftp-rmdir", ResourceManager.getIcon("resources/icons/ftp.gif"));
    }

    private DefaultMutableTreeNode node;
    private IElementDef elementDef;
    private int executionCount = 0;
    private Exception exception = null;
    private Map properties;

    // list of synchronizes views
    private List synchronizedViews = new ArrayList();

    public TreeNodeInfo(IElementDef elementDef) {
        this.elementDef = elementDef;
        this.node = new DefaultMutableTreeNode(this);
    }

    public IElementDef getElementDef() {
        return elementDef;
    }

    public DefaultMutableTreeNode getNode() {
        return node;
    }

    public Icon getIcon() {
        Icon result;
        if (this.elementDef == null) {
            result = DEFAULT_ICON;
        } else {
            result = (Icon) icons.get(this.elementDef.getClass());
            if (result == null && elementDef instanceof WebHarvestPluginDef) {
                result = (Icon) icons.get( ((WebHarvestPluginDef)elementDef).getShortElementName() );
                if (result == null) {
                    result = ResourceManager.getIcon("resources/icons/plugin.gif");
                }
            }
            if (result == null) {
                result = DEFAULT_ICON;
            }
        }

        return result;
    }

    public String toString() {
        if (this.elementDef == null) {
            return "";
        }

        String result = this.elementDef.getShortElementName();
        if ( this.executionCount > 0 ) {
            result +=  " [" + this.executionCount + "]";
        }
        return result;
    }

    public void increaseExecutionCount() {
        this.executionCount++;
    }

    public int getExecutionCount() {
        return executionCount;
    }

    public void setException(Exception e) {
        this.exception = e;
    }

    public boolean hasException() {
        return this.exception != null;
    }

    public Exception getException() {
        return exception;
    }

    public void setProperties(Map properties) {
        this.properties = properties;
    }

    public Map getProperties() {
        return properties;
    }

    public List getSynchronizedViews() {
        return synchronizedViews;
    }

    public void removeSynchronizedView(ViewerFrame viewerFrame) {
        this.synchronizedViews.remove(viewerFrame);
    }

    public void addSynchronizedView(ViewerFrame viewerFrame) {
        this.synchronizedViews.add(viewerFrame);
    }
    
}