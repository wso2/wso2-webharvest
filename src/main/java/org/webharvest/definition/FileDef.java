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
package org.webharvest.definition;

import org.webharvest.utils.CommonUtil;

/**
 * Definition of file proessor.
 */
public class FileDef extends BaseElementDef {

    private String action;
    private String path;
    private String type;
    private String charset;
    private String listFilter;
    private String listFiles;
    private String listDirs;
    private String listRecursive;

    public FileDef(XmlNode xmlNode) {
        super(xmlNode);

        this.action = xmlNode.getAttribute("action");
        this.path = CommonUtil.adaptFilename( xmlNode.getAttribute("path") );
        this.type = xmlNode.getAttribute("type");
        this.charset = xmlNode.getAttribute("charset");
        this.listFilter = xmlNode.getAttribute("listfilter");
        this.listFiles = xmlNode.getAttribute("listfiles");
        this.listDirs = xmlNode.getAttribute("listdirs");
        this.listRecursive = xmlNode.getAttribute("listrecursive");
    }

    public String getAction() {
        return action;
    }

    public String getPath() {
        return CommonUtil.adaptFilename(path);
    }

    public String getType() {
        return type;
    }

    public String getCharset() {
        return charset;
    }

    public String getShortElementName() {
        return "file";
    }

    public String getListFilter() {
        return listFilter;
    }

    public String getListDirs() {
        return listDirs;
    }

    public String getListFiles() {
        return listFiles;
    }

    public String getListRecursive() {
        return listRecursive;
    }
    
}