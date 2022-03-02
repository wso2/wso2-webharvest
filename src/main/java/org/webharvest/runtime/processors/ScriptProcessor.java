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

import org.webharvest.definition.ScriptDef;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.scripting.ScriptEngine;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

/**
 * Script processor - executes script defined in the body and optionally returns result.
 */
public class ScriptProcessor extends BaseProcessor {

    public static final String CONTEXT_VARIABLE_NAME = "context";

    private ScriptDef scriptDef;

    public ScriptProcessor(ScriptDef scriptDef) {
        super(scriptDef);
        this.scriptDef = scriptDef;
    }

    public Variable execute(Scraper scraper, ScraperContext context) {
        Variable scriptText = getBodyTextContent(scriptDef, scraper, context);

        String language = BaseTemplater.execute( scriptDef.getLanguage(), scraper.getScriptEngine());
        if (language != null) {
            language = language.toLowerCase();
        }

        String returnExpression = scriptDef.getReturnExpression();

        ScriptEngine scriptEngine = language == null ? scraper.getScriptEngine() : scraper.getScriptEngine(language);
        scriptEngine.eval( scriptText.toString() );

        if (returnExpression != null) {
            String returnExpressionEvaluated = BaseTemplater.execute( scriptDef.getReturnExpression(), scraper.getScriptEngine());
            Object returnValue = scriptEngine.eval(returnExpressionEvaluated);
            return CommonUtil.createVariable(returnValue);
        } else {
            return new EmptyVariable();
        }
        
    }

}