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

import org.webharvest.utils.*;
import org.webharvest.gui.*;

import java.util.*;

public class BaseElementDef implements IElementDef {

    // sequence of operation definitions
    List operationDefs = new ArrayList();

    // text content if no nested operation definitions
    String body;

    // element ID
    String id;

    // descriptive name
    private String descName = "*";

    // location of this element in source XML
    private int lineNumber;
    private int columnNumber;

    protected BaseElementDef() {
    }
    
    protected BaseElementDef(XmlNode node) {
    	this(node, true);
    }

    protected BaseElementDef(XmlNode node, String descName) {
    	this(node);
        this.descName = descName;
    }

    protected BaseElementDef(XmlNode node, boolean createBodyDefs) {
        if (node != null) {
            this.lineNumber = node.getLineNumber();
            this.columnNumber = node.getColumnNumber();

            this.id = (String) node.get("id");

            List elementList = node.getElementList();

            if (createBodyDefs) {
	            if (elementList != null && elementList.size() > 0) {
	                Iterator it = elementList.iterator();
	                while (it.hasNext()) {
	                    Object element = it.next();
	                    if (element instanceof XmlNode) {
	                        XmlNode currElementNode = (XmlNode) element;
	                        IElementDef def = DefinitionResolver.createElementDefinition(currElementNode);
	                        if (def != null) {
	                            operationDefs.add(def);
	                        }
	                    } else {
	                        operationDefs.add( new ConstantDef(element.toString()) );
	                    }
	                }
	            } else {
	                body = node.getText();
	            }
            }
        }
    }

    public boolean hasOperations() {
        return operationDefs != null && operationDefs.size() > 0;
    }

    public IElementDef[] getOperationDefs() {
        IElementDef[] defs = new IElementDef[operationDefs.size()];
        Iterator it = operationDefs.iterator();
        int index = 0;
        while (it.hasNext()) {
            defs[index++] = (IElementDef) it.next();
        }

        return defs;
    }

    public String getBodyText() {
        return body;
    }

    public String getId() {
        return id;
    }

    public String getShortElementName() {
        return descName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

}