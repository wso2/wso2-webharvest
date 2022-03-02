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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.webharvest.exception.ParserException;
import org.webharvest.utils.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.LocatorImpl;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.StringTokenizer;


public class XmlParser extends DefaultHandler {

    protected static Log log = LogFactory.getLog(XmlParser.class);

    XmlNode root;

    // working stack of elements
    private transient Stack elementStack = new Stack();
    private Locator locator;

    public static XmlNode parse(InputSource in) {
        long startTime = System.currentTimeMillis();

        XmlParser handler = new XmlParser();
        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setValidating(false);
            parserFactory.setNamespaceAware(false);

            SAXParser parser = parserFactory.newSAXParser();

            // call parsing
            parser.parse(in, handler);

            log.info("XML parsed in " + (System.currentTimeMillis() - startTime) + "ms.");
        } catch (Exception e) {
            throw new ParserException(e.getMessage(), e);
        }

        return handler.root;
    }

    public XmlParser() {
        this.setDocumentLocator(new LocatorImpl());
    }


    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    private XmlNode getCurrentNode() {
        return elementStack.size() > 0 ? (XmlNode) elementStack.peek() : null;
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        XmlNode currNode = getCurrentNode();
        if (currNode != null) {
            currNode.addElement( new String(ch, start, length) );
        }
    }
    
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        XmlNode currNode = getCurrentNode();
        XmlNode newNode = new XmlNode(qName, currNode);
        newNode.setLocation( this.locator.getLineNumber(), this.locator.getColumnNumber() );
        elementStack.push(newNode);

        if (currNode == null) {
            root = newNode;
        }

        int attsCount = attributes.getLength();
        for (int i = 0; i < attsCount; i++) {
            newNode.addAttribute( attributes.getQName(i), attributes.getValue(i) );
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (elementStack.size() > 0) {
            getCurrentNode().flushText();
            elementStack.pop();
        }
    }

}