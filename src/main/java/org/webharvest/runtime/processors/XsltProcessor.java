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
package org.webharvest.runtime.processors;

import org.webharvest.definition.BaseElementDef;
import org.webharvest.definition.XsltDef;
import org.webharvest.exception.XsltException;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.runtime.variables.NodeVariable;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * XSLT processor.
 */
public class XsltProcessor extends BaseProcessor {

    private XsltDef xsltDef;

    public XsltProcessor(XsltDef xsltDef) {
        super(xsltDef);
        this.xsltDef = xsltDef;
    }

    public Variable execute(Scraper scraper, ScraperContext context) {
        BaseElementDef xsltElementDef = xsltDef.getXmlDef();
        Variable xmlStr = getBodyTextContent(xsltElementDef, scraper, context, true);
        debug(xsltElementDef, scraper, xmlStr);

        BaseElementDef stylesheetElementDef = xsltDef.getStylesheetDef();
        Variable stylesheetStr = getBodyTextContent(stylesheetElementDef, scraper, context, true);
        debug(stylesheetElementDef, scraper, stylesheetStr);
    	
        try {
    		TransformerFactory xformFactory = TransformerFactory.newInstance();
    		Source xsl = new StreamSource( new StringReader(stylesheetStr.toString()) );
    		Transformer stylesheet = xformFactory.newTransformer(xsl);
    		Source request  = new StreamSource( new StringReader(xmlStr.toString()) );
    		StringWriter writer = new StringWriter();
    		Result response = new StreamResult(writer);
    		stylesheet.transform(request, response);
    		
    		return new NodeVariable(writer.toString()); 
    	} catch (TransformerException e) {
    		throw new XsltException("Error during XSLT transforming!", e);
    	}
    }

}