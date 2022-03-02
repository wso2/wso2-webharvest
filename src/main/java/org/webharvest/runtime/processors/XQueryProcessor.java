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

import net.sf.saxon.Configuration;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;
import org.webharvest.definition.BaseElementDef;
import org.webharvest.definition.XQueryDef;
import org.webharvest.definition.XQueryExternalParamDef;
import org.webharvest.exception.ScraperXQueryException;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.RuntimeConfig;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.utils.CommonUtil;
import org.webharvest.utils.KeyValuePair;
import org.webharvest.utils.XmlUtil;

import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.util.*;

/**
 * XQuery processor.
 */
public class XQueryProcessor extends BaseProcessor {

    public static Set ALLOWED_PARAM_TYPES = new TreeSet();
    public static String DEFAULT_PARAM_TYPE = "node()";

    // initialize set of allowed parameter types
    static {
        ALLOWED_PARAM_TYPES.add("node()");
        ALLOWED_PARAM_TYPES.add("node()*");
        ALLOWED_PARAM_TYPES.add("integer");
        ALLOWED_PARAM_TYPES.add("integer*");
        ALLOWED_PARAM_TYPES.add("long");
        ALLOWED_PARAM_TYPES.add("long*");
        ALLOWED_PARAM_TYPES.add("float");
        ALLOWED_PARAM_TYPES.add("float*");
        ALLOWED_PARAM_TYPES.add("double");
        ALLOWED_PARAM_TYPES.add("double*");
        ALLOWED_PARAM_TYPES.add("boolean");
        ALLOWED_PARAM_TYPES.add("boolean*");
        ALLOWED_PARAM_TYPES.add("string");
        ALLOWED_PARAM_TYPES.add("string*");
    }

    private XQueryDef xqueryDef;

    public XQueryProcessor(XQueryDef xqueryDef) {
        super(xqueryDef);
        this.xqueryDef = xqueryDef;
    }

    public Variable execute(Scraper scraper, ScraperContext context) {
        BaseElementDef xqueryElementDef = xqueryDef.getXqDef();
        Variable xq = getBodyTextContent(xqueryElementDef, scraper, context, true);
        debug(xqueryElementDef, scraper, xq);

        String xqExpression = xq.toString().trim();
        XQueryExternalParamDef[] externalParamDefs = xqueryDef.getExternalParamDefs();

        RuntimeConfig runtimeConfig = scraper.getRuntimeConfig();
        final StaticQueryContext sqc = runtimeConfig.getStaticQueryContext();
        final Configuration config = sqc.getConfiguration();

	    try {
	        final XQueryExpression exp = runtimeConfig.getXQueryExpressionPool().getCompiledExpression(xqExpression);
		    final DynamicQueryContext dynamicContext = new DynamicQueryContext(config);

            // define external parameters
            for (int i = 0; i < externalParamDefs.length; i++) {
                XQueryExternalParamDef externalParamDef = externalParamDefs[i];
                String externalParamName = BaseTemplater.execute( externalParamDef.getName(), scraper.getScriptEngine() );
                String externalParamType = BaseTemplater.execute( externalParamDefs[i].getType(), scraper.getScriptEngine() );
                if (externalParamType == null) {
                    externalParamType = DEFAULT_PARAM_TYPE;
                }

                // check if param type is one of allowed
                if ( !ALLOWED_PARAM_TYPES.contains(externalParamType) ) {
                    throw new ScraperXQueryException("Type " + externalParamType + " is not allowed. Use one of " + ALLOWED_PARAM_TYPES.toString());
                }

                if ( externalParamType.endsWith("*") ) {
                    BodyProcessor bodyProcessor = new BodyProcessor(externalParamDef);
                    bodyProcessor.setProperty("Name", externalParamName);
                    bodyProcessor.setProperty("Type", externalParamType);
                    ListVariable listVar = (ListVariable) bodyProcessor.run(scraper, context);
                    debug(externalParamDef, scraper, listVar);
                    
                    Iterator it = listVar.toList().iterator();
                    List paramList = new ArrayList(); 
                    while (it.hasNext()) {
                        Variable currVar =  (Variable) it.next();
                        paramList.add( castSimpleValue(externalParamType, currVar, sqc) );
                    }

                    dynamicContext.setParameter(externalParamName, paramList);
                } else {
                    KeyValuePair props[] = {new KeyValuePair("Name", externalParamName), new KeyValuePair("Type", externalParamType)}; 
                    Variable var = getBodyTextContent(externalParamDef, scraper, context, true, props);

                    debug(externalParamDef, scraper, var);
                    
                    Object value = castSimpleValue(externalParamType, var, sqc);
                    dynamicContext.setParameter(externalParamName, value);
                }
            }

	        return XmlUtil.createListOfXmlNodes(exp, dynamicContext);
	    } catch (XPathException e) {
	    	throw new ScraperXQueryException("Error executing XQuery expression (XQuery = [" + xqExpression + "])!", e);
	    }
    }

    /**
     * For the specified type, value and static query context, returns proper Java typed value. 
     * @param type
     * @param value
     * @param sqc
     * @return
     * @throws XPathException
     */
    private Object castSimpleValue(String type, Variable value, StaticQueryContext sqc) throws XPathException {
        type = type.toLowerCase();

        if ( type.startsWith("node()") ) {
            StringReader reader = new StringReader(value.toString() );
            return sqc.buildDocument(new StreamSource(reader));
        } else if ( type.startsWith("integer") ) {
            return new Integer( value.toString().trim() );
        } else if ( type.startsWith("long") ) {
            return new Long( value.toString().trim() );
        } else if ( type.startsWith("float") ) {
            return new Float( value.toString().trim() );
        } else if ( type.startsWith("double") ) {
            return new Double( value.toString().trim() );
        } else if ( type.startsWith("boolean") ) {
            return CommonUtil.isBooleanTrue(value.toString()) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            return value.toString();
        }
    }

}