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
import org.webharvest.definition.RegexpDef;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.scripting.ScriptEngine;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.utils.CommonUtil;
import org.webharvest.utils.Constants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regular expression replace processor.
 */
public class RegexpProcessor extends BaseProcessor {

    private RegexpDef regexpDef;

    public RegexpProcessor(RegexpDef regexpDef) {
        super(regexpDef);
        this.regexpDef = regexpDef;
    }

    public Variable execute(Scraper scraper, ScraperContext context) {
        ScriptEngine scriptEngine = scraper.getScriptEngine();

        BaseElementDef patternDef = regexpDef.getRegexpPatternDef();
        Variable patternVar = getBodyTextContent(patternDef, scraper, context, true);
        debug(patternDef, scraper, patternVar);

        BaseElementDef sourceDef = regexpDef.getRegexpSourceDef();
        Variable source = new BodyProcessor(sourceDef).run(scraper, context);
        debug(sourceDef, scraper, source);
        
        String replace = BaseTemplater.execute( regexpDef.getReplace(), scriptEngine);
        boolean isReplace = CommonUtil.isBooleanTrue(replace);

        boolean flagCaseInsensitive = CommonUtil.getBooleanValue( BaseTemplater.execute(regexpDef.getFlagCaseInsensitive(), scriptEngine), false );
        boolean flagMultiline = CommonUtil.getBooleanValue( BaseTemplater.execute(regexpDef.getFlagMultiline(), scriptEngine), false );
        boolean flagDotall = CommonUtil.getBooleanValue( BaseTemplater.execute(regexpDef.getFlagDotall(), scriptEngine), true );
        boolean flagUnicodecase = CommonUtil.getBooleanValue( BaseTemplater.execute(regexpDef.getFlagUnicodecase(), scriptEngine), true );
        boolean flagCanoneq = CommonUtil.getBooleanValue( BaseTemplater.execute(regexpDef.getFlagCanoneq(), scriptEngine), false );

        this.setProperty("Is replacing", String.valueOf(isReplace));
        this.setProperty("Flag CaseInsensitive", String.valueOf(flagCaseInsensitive));
        this.setProperty("Flag MultiLine", String.valueOf(flagMultiline));
        this.setProperty("Flag DotAll", String.valueOf(flagDotall));
        this.setProperty("Flag UnicodeCase", String.valueOf(flagUnicodecase));
        this.setProperty("Flag CanonEq", String.valueOf(flagCanoneq));

        String maxLoopsString = BaseTemplater.execute( regexpDef.getMax(), scriptEngine);
        double maxLoops = Constants.DEFAULT_MAX_LOOPS;
        if (maxLoopsString != null && !"".equals(maxLoopsString.trim())) {
            maxLoops = Double.parseDouble(maxLoopsString);
        }

        this.setProperty("Max loops", String.valueOf(maxLoops));

        int flags = 0;
        if (flagCaseInsensitive) {
            flags |= Pattern.CASE_INSENSITIVE;
        }
        if (flagMultiline) {
            flags |= Pattern.MULTILINE;
        }
        if (flagDotall) {
            flags |= Pattern.DOTALL;
        }
        if (flagUnicodecase) {
            flags |= Pattern.UNICODE_CASE;
        }
        if (flagCanoneq) {
            flags |= Pattern.CANON_EQ;
        }

        Pattern pattern = Pattern.compile(patternVar.toString(), flags);
        
        List resultList = new ArrayList();
        
        List bodyList = source.toList();
        Iterator it = bodyList.iterator();
        while (it.hasNext()) {
        	Variable currVar = (Variable) it.next();
        	String text = currVar.toString();
            
            Matcher matcher = pattern.matcher(text);
            int groupCount = matcher.groupCount();
            
            StringBuffer buffer = new StringBuffer();
            
            int index = 0; 
            while ( matcher.find() ) {
            	index++;

            	// if index exceeds maximum number of matching sequences exists the loop
                if (maxLoops < index) {
                    break;
                }

            	for (int i = 0; i <= groupCount; i++) {
            		context.put("_"+i, new NodeVariable(matcher.group(i)));
            	}

                BaseElementDef resultDef = regexpDef.getRegexpResultDef();
                Variable result = getBodyTextContent(resultDef, scraper, context, true);
                debug(resultDef, scraper, result);
                
                String currResult = (result == null) ? matcher.group(0) : result.toString();
            	if (isReplace) {
            		matcher.appendReplacement(buffer, currResult);
            	} else {
            		resultList.add(new NodeVariable(currResult));
            	}
            	
            	for (int i = 0; i <= groupCount; i++) {
            		context.remove("_"+i);
            	}
            }
            
            if (isReplace) {
        		matcher.appendTail(buffer);
        		resultList.add(new NodeVariable(buffer.toString()));
        	}
        }
        

        return new ListVariable(resultList);
    }

}