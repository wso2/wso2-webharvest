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

/**
 * Definition of loop processor.
 */
public class LoopDef extends BaseElementDef {

    private String maxloops;
    private String item;
    private String index;
    private String filter;
    private String empty;
    private BaseElementDef loopValueDef;
    private BaseElementDef loopBodyDef;

    public LoopDef(XmlNode xmlNode) {
        super(xmlNode, false);

        XmlNode loopValueDefNode = (XmlNode) xmlNode.get("list[0]");
        DefinitionResolver.validate(loopValueDefNode);
        this.loopValueDef = loopValueDefNode == null ? null : new BaseElementDef(loopValueDefNode, "list");

        XmlNode loopBodyDefNode = (XmlNode) xmlNode.get("body[0]");
        DefinitionResolver.validate(loopBodyDefNode);
        this.loopBodyDef = loopBodyDefNode == null ? null : new BaseElementDef(loopBodyDefNode, "body");

        this.maxloops = xmlNode.getAttribute("maxloops");
        this.item = xmlNode.getAttribute("item");
        this.index = xmlNode.getAttribute("index");
        this.filter = xmlNode.getAttribute("filter");
        this.empty = xmlNode.getAttribute("empty");
    }

    public String getMaxloops() {
        return maxloops;
    }

    public String getItem() {
        return item;
    }

    public String getIndex() {
        return index;
    }

    public String getFilter() {
        return filter;
    }

    public String getEmpty() {
        return empty;
    }

    public BaseElementDef getLoopValueDef() {
        return loopValueDef;
    }

    public BaseElementDef getLoopBodyDef() {
        return loopBodyDef;
    }

    public IElementDef[] getOperationDefs() {
        return new IElementDef[] {this.loopValueDef, this.loopBodyDef};
    }

    public String getShortElementName() {
        return "loop";
    }

}