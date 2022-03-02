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

import org.webharvest.definition.WhileDef;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.scripting.ScriptEngine;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.*;
import org.webharvest.utils.CommonUtil;
import org.webharvest.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Conditional processor.
 */
public class WhileProcessor extends BaseProcessor {

    private WhileDef whileDef;

    public WhileProcessor(WhileDef whileDef) {
        super(whileDef);
        this.whileDef = whileDef;
    }

    public Variable execute(Scraper scraper, ScraperContext context) {
        ScriptEngine scriptEngine = scraper.getScriptEngine();
        String index = BaseTemplater.execute( whileDef.getIndex(), scriptEngine);
        String maxLoopsString = BaseTemplater.execute( whileDef.getMaxLoops(), scriptEngine);
        boolean isEmpty = CommonUtil.getBooleanValue( BaseTemplater.execute(whileDef.getEmpty(), scriptEngine), false );

        double maxLoops = Constants.DEFAULT_MAX_LOOPS;
        if (maxLoopsString != null && !"".equals(maxLoopsString.trim())) {
            maxLoops = Double.parseDouble(maxLoopsString);
        }

        List resultList = new ArrayList();

        Variable indexBeforeLoop = (Variable) context.get(index);

        int i = 1;

        // define first value of index variable
        if ( index != null && !"".equals(index) ) {
            context.put( index, new NodeVariable(String.valueOf(i)) );
        }

        String condition = BaseTemplater.execute( whileDef.getCondition(), scriptEngine);

        this.setProperty("Condition", condition);
        this.setProperty("Index", index);
        this.setProperty("Max Loops", maxLoopsString);
        this.setProperty("Empty", String.valueOf(isEmpty));

        // iterates while testing variable represents boolean true or loop limit is exceeded
        while ( CommonUtil.isBooleanTrue(condition) && (i <= maxLoops) ) {
            Variable loopResult = new BodyProcessor(whileDef).execute(scraper, context);
            if (!isEmpty) {
                resultList.addAll( loopResult.toList() );
            }

            i++;
            // define current value of index variable
            if ( index != null && !"".equals(index) ) {
                context.put( index, new NodeVariable(String.valueOf(i)) );
            }

            condition = BaseTemplater.execute(whileDef.getCondition(), scriptEngine);
        }

        // restores previous value of index variable
        if (index != null && indexBeforeLoop != null) {
            context.put(index, indexBeforeLoop);
        }

        return isEmpty ? new EmptyVariable() : new ListVariable(resultList);
    }

}