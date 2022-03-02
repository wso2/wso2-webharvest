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

import org.webharvest.definition.*;
import org.webharvest.runtime.Scraper;

public class ProcessorResolver {
	
    public static BaseProcessor createProcessor(IElementDef elementDef, ScraperConfiguration configuration, Scraper scraper) {
        if (elementDef instanceof EmptyDef) {
            return new EmptyProcessor( (EmptyDef)elementDef );
        } else if (elementDef instanceof TextDef) {
        	return new TextProcessor( (TextDef)elementDef );
        } else if (elementDef instanceof ConstantDef) {
        	return new ConstantProcessor( (ConstantDef)elementDef );
        } else if (elementDef instanceof FileDef) {
        	return new FileProcessor( (FileDef)elementDef );
	    } else if (elementDef instanceof VarDefDef) {
	    	return new VarDefProcessor( (VarDefDef)elementDef );
	    } else if (elementDef instanceof VarDef) {
	    	return new VarProcessor( (VarDef)elementDef );
	    } else if (elementDef instanceof HttpDef) {
	    	return new HttpProcessor( (HttpDef)elementDef );
	    } else if (elementDef instanceof HttpParamDef) {
	    	return new HttpParamProcessor( (HttpParamDef)elementDef );
	    } else if (elementDef instanceof HttpHeaderDef) {
	    	return new HttpHeaderProcessor( (HttpHeaderDef)elementDef );
	    } else if (elementDef instanceof XPathDef) {
	    	return new XPathProcessor( (XPathDef)elementDef );
	    } else if (elementDef instanceof XQueryDef) {
	    	return new XQueryProcessor( (XQueryDef)elementDef );
	    } else if (elementDef instanceof XsltDef) {
	    	return new XsltProcessor( (XsltDef)elementDef );
	    } else if (elementDef instanceof TemplateDef) {
	    	return new TemplateProcessor( (TemplateDef)elementDef );
	    } else if (elementDef instanceof RegexpDef) {
	    	return new RegexpProcessor( (RegexpDef)elementDef );
	    } else if (elementDef instanceof HtmlToXmlDef) {
	    	return new HtmlToXmlProcessor( (HtmlToXmlDef)elementDef );
	    } else if (elementDef instanceof CaseDef) {
            return new CaseProcessor( (CaseDef)elementDef );
	    } else if (elementDef instanceof LoopDef) {
	    	return new LoopProcessor( (LoopDef)elementDef );
	    } else if (elementDef instanceof WhileDef) {
	    	return new WhileProcessor( (WhileDef)elementDef );
	    } else if (elementDef instanceof FunctionDef) {
	    	return new FunctionProcessor( (FunctionDef)elementDef );
	    } else if (elementDef instanceof IncludeDef) {
	    	return new IncludeProcessor( (IncludeDef)elementDef );
	    } else if (elementDef instanceof CallDef) {
	    	return new CallProcessor( (CallDef)elementDef, configuration, scraper );
	    } else if (elementDef instanceof CallParamDef) {
	    	return new CallParamProcessor( (CallParamDef)elementDef );
	    } else if (elementDef instanceof ReturnDef) {
	    	return new ReturnProcessor( (ReturnDef)elementDef );
	    } else if (elementDef instanceof TryDef) {
	    	return new TryProcessor( (TryDef)elementDef );
	    } else if (elementDef instanceof ScriptDef) {
	    	return new ScriptProcessor( (ScriptDef)elementDef );
	    } else if (elementDef instanceof ExitDef) {
	    	return new ExitProcessor( (ExitDef)elementDef );
	    } else if (elementDef instanceof WebHarvestPluginDef) {
            WebHarvestPluginDef pluginDef = (WebHarvestPluginDef) elementDef;
            return pluginDef.createPlugin();
        }

        return null;
    }
    
}