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

import org.webharvest.utils.Catalog;
import org.webharvest.runtime.scripting.ScriptEngine;
import org.webharvest.runtime.scripting.BeanShellScriptEngine;
import org.webharvest.runtime.scripting.JavascriptScriptEngine;
import org.webharvest.runtime.scripting.GroovyScriptEngine;
import org.xml.sax.InputSource;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Basic configuration.
 */
public class ScraperConfiguration {

    public static final String BEANSHELL_SCRIPT_ENGINE = "beanshell";
    public static final String JAVASCRIPT_SCRIPT_ENGINE = "javascript";
    public static final String GROOVY_SCRIPT_ENGINE = "groovy";

    public static final String DEFAULT_CHARSET = "UTF-8";

    // map of function definitions
    private Map functionDefs = new Catalog();

    // sequence of operationDefs
    private List operations = new ArrayList();
    
    private String charset = DEFAULT_CHARSET;
    private String defaultScriptEngine = BEANSHELL_SCRIPT_ENGINE;

    private File sourceFile;
    private String url;

    /**
     * Creates configuration instance loaded from the specified input stream.
     * 
     * @param in
     */
    public ScraperConfiguration(InputSource in) {
        createFromInputStream(in);

    }

    private void createFromInputStream(InputSource in) {
        // loads configuration from input stream to the internal structure
        XmlNode node = XmlNode.getInstance(in);

        String charsetString = node.getString("charset");
        this.charset = charsetString != null ? charsetString : DEFAULT_CHARSET;

        String scriptEngineDesc = node.getString("scriptlang");
        if ( "javascript".equalsIgnoreCase(scriptEngineDesc) ) {
            this.defaultScriptEngine = JAVASCRIPT_SCRIPT_ENGINE;
        } else if ( "groovy".equalsIgnoreCase(scriptEngineDesc) ) {
            this.defaultScriptEngine = GROOVY_SCRIPT_ENGINE;
        } else {
            this.defaultScriptEngine = BEANSHELL_SCRIPT_ENGINE;
        }

        List elementList = node.getElementList();
        Iterator it = elementList.iterator();
        while (it.hasNext()) {
            Object element = it.next();
            if (element instanceof XmlNode) {
                XmlNode currElementNode = (XmlNode) element;
                operations.add( DefinitionResolver.createElementDefinition(currElementNode) );
            } else {
                operations.add( new ConstantDef(element.toString()) );
            }
        }
    }

    /**
     * Creates configuration instance loaded from the specified File.
     * 
     * @param sourceFile
     * @throws FileNotFoundException
     */
    public ScraperConfiguration(File sourceFile) throws FileNotFoundException {
        this.sourceFile = sourceFile;
        createFromInputStream( new InputSource(new FileReader(sourceFile)) );
    }

    /**
     * Creates configuration instance loaded from the file specified by filename.
     * 
     * @param sourceFilePath
     */
    public ScraperConfiguration(String sourceFilePath) throws FileNotFoundException {
        this( new File(sourceFilePath) );
    }

    /**
     * Creates configuration instance loaded from specified URL.
     *  
     * @param sourceUrl
     * @throws IOException
     */
    public ScraperConfiguration(URL sourceUrl) throws IOException {
        this.url = sourceUrl.toString();
        createFromInputStream( new InputSource(new InputStreamReader(sourceUrl.openStream())) );
    }

    public List getOperations() {
        return operations;
    }

	public String getCharset() {
		return charset;
	}

    public String getDefaultScriptEngine() {
        return defaultScriptEngine;
    }

    public FunctionDef getFunctionDef(String name) {
        return (FunctionDef) functionDefs.get(name);
    }

    public void addFunctionDef(FunctionDef funcDef) {
        functionDefs.put(funcDef.getName(), funcDef);
    }

    public File getSourceFile() {
        return this.sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ScriptEngine createScriptEngine(Map context, String engineType) {
        if ( JAVASCRIPT_SCRIPT_ENGINE.equalsIgnoreCase(engineType) ) {
            return new JavascriptScriptEngine(context);
        } else if ( GROOVY_SCRIPT_ENGINE.equalsIgnoreCase(engineType) ) {
            return new GroovyScriptEngine(context);
        } else {
            return new BeanShellScriptEngine(context);
        }
    }

    public ScriptEngine createScriptEngine(Map context) {
        return createScriptEngine(context, this.defaultScriptEngine);
    }

}