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

import org.webharvest.definition.CallDef;
import org.webharvest.definition.FunctionDef;
import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.exception.FunctionException;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.scripting.ScriptEngine;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.runtime.variables.NodeVariable;

/**
 * Function call processor.
 */
public class CallProcessor extends BaseProcessor {

    private CallDef callDef;
    
    ScraperContext functionContext;
    ScriptEngine scriptEngine;

    private Variable functionResult = new NodeVariable("");

    public CallProcessor(CallDef callDef, ScraperConfiguration configuration, Scraper scraper) {
        super(callDef);
        CallProcessor runningFunction = scraper.getRunningFunction();
        ScraperContext callerContext =
                runningFunction == null ? scraper.getContext() : runningFunction.getFunctionContext();
        this.functionContext = new ScraperContext(scraper, callerContext);
        this.scriptEngine = configuration.createScriptEngine(functionContext);
        this.callDef = callDef;
    }

    public Variable execute(Scraper scraper, ScraperContext context) {
        String functionName = BaseTemplater.execute( callDef.getName(), scraper.getScriptEngine() );
        FunctionDef functionDef = scraper.getConfiguration().getFunctionDef(functionName);

        this.setProperty("Name", functionName);

        if (functionDef == null) {
            throw new FunctionException("Function \"" + functionName + "\" is undefined!");
        }

        scraper.clearFunctionParams();

        // executes body of call processor
        new BodyProcessor(callDef).execute(scraper, context);

        functionContext.putAll( scraper.getFunctionParams() );

        // adds this runtime info to the running functions stack
        scraper.addRunningFunction(this);
        
        // executes body of function using new context
        new BodyProcessor(functionDef).execute(scraper, functionContext);

        // remove running function from the stack  
        scraper.removeRunningFunction();

        return functionResult;
    }

    public void setFunctionResult(Variable result) {
        this.functionResult = result;
    }
    
    public ScriptEngine getScriptEngine() {
        return scriptEngine;
    }

    public ScraperContext getFunctionContext() {
        return functionContext;
    }
    
}