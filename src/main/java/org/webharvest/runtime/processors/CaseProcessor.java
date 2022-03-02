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
import org.webharvest.definition.CaseDef;
import org.webharvest.definition.IfDef;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

/**
 * Conditional processor.
 */
public class CaseProcessor extends BaseProcessor {

    private CaseDef caseDef;

    public CaseProcessor(CaseDef caseDef) {
        super(caseDef);
        this.caseDef = caseDef;
    }

    public Variable execute(Scraper scraper, ScraperContext context) {
        IfDef[] ifDefs = caseDef.getIfDefs();
        
        if (ifDefs != null) {
        	for (int i = 0; i < ifDefs.length; i++) {
        		String condition = BaseTemplater.execute( ifDefs[i].getCondition(), scraper.getScriptEngine() );
        		if ( CommonUtil.isBooleanTrue(condition) ) {
        			Variable ifResult = new BodyProcessor(ifDefs[i]).run(scraper, context);
                    debug(ifDefs[i], scraper, ifResult);
                    return ifResult;
                }
        	}
        }

        BaseElementDef elseDef = caseDef.getElseDef();
        if (elseDef != null) {
        	Variable elseResult = new BodyProcessor(elseDef).run(scraper, context);
            debug(elseDef, scraper, elseResult);
            return elseResult;
        }
        
        return new EmptyVariable();
    }

}