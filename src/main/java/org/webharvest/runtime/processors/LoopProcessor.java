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
import org.webharvest.definition.LoopDef;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.scripting.ScriptEngine;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.utils.CommonUtil;
import org.webharvest.utils.Constants;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loop list processor.
 */
public class LoopProcessor extends BaseProcessor {

    private LoopDef loopDef;

    public LoopProcessor(LoopDef loopDef) {
        super(loopDef);
        this.loopDef = loopDef;
    }

    public Variable execute(Scraper scraper, ScraperContext context) {
        ScriptEngine scriptEngine = scraper.getScriptEngine();
        String item = BaseTemplater.execute( loopDef.getItem(), scriptEngine);
        String index = BaseTemplater.execute( loopDef.getIndex(), scriptEngine);
        String maxLoopsString = BaseTemplater.execute( loopDef.getMaxloops(), scriptEngine);
        String filter = BaseTemplater.execute( loopDef.getFilter(), scriptEngine);
        boolean isEmpty = CommonUtil.getBooleanValue( BaseTemplater.execute(loopDef.getEmpty(), scriptEngine), false );

        this.setProperty("Item", item);
        this.setProperty("Index", index);
        this.setProperty("Max Loops", maxLoopsString);
        this.setProperty("Filter", filter);
        this.setProperty("Empty", String.valueOf(isEmpty));

        double maxLoops = Constants.DEFAULT_MAX_LOOPS;
        if (maxLoopsString != null && !"".equals(maxLoopsString.trim())) {
            maxLoops = Double.parseDouble(maxLoopsString);
        }

        BaseElementDef loopValueDef = loopDef.getLoopValueDef();
        Variable loopValue = new BodyProcessor(loopValueDef).run(scraper, context);
        debug(loopValueDef, scraper, loopValue);

        List resultList = new ArrayList();

        List list = loopValue != null ? loopValue.toList() : null;
        if (list != null) {
            Variable itemBeforeLoop = (Variable) context.get(item);
            Variable indexBeforeLoop = (Variable) context.get(index);

            List filteredList = filter != null ? createFilteredList(list, filter) : list;
            Iterator it = filteredList.iterator();

            for (int i = 1; it.hasNext() && i <= maxLoops; i++) {
                Variable currElement = (Variable) it.next();

                // define current value of item variable
                if ( item != null && !"".equals(item) ) {
                    context.put(item, currElement);
                }

                // define current value of index variable
                if ( index != null && !"".equals(index) ) {
                    context.put( index, new NodeVariable(String.valueOf(i)) );
                }

                // execute the loop body
                BaseElementDef bodyDef = loopDef.getLoopBodyDef();
                Variable loopResult = bodyDef != null ? new BodyProcessor(bodyDef).run(scraper, context) : new EmptyVariable();
                debug(bodyDef, scraper, loopResult);
                if (!isEmpty) {
                    resultList.addAll( loopResult.toList() );
                }
            }

            // restores previous value of item variable
            if (item != null && itemBeforeLoop != null) {
                context.put(item, itemBeforeLoop);
            }

            // restores previous value of index variable
            if (index != null && indexBeforeLoop != null) {
                context.put(index, indexBeforeLoop);
            }
        }

        return isEmpty ? new EmptyVariable() : new ListVariable(resultList);
    }

    /**
     * Create filtered list based on specified list and filterStr
     * @param list
     * @param filterStr
     * @return Filtered list
     */
    private List createFilteredList(List list, String filterStr) {
        List result = new ArrayList();
        Set stringSet = new HashSet();

        Filter filter = new Filter(filterStr, list.size());

        Iterator it = list.iterator();
        int index = 1;
        while (it.hasNext()) {
            Variable curr = (Variable) it.next();

            if (filter.isInFilter(index)) {
                if (filter.isUnique) {
                    String currStr = curr.toString();
                    if (!stringSet.contains(curr.toString())) {
                        result.add(curr);
                        stringSet.add(currStr);
                    }
                } else {
                    result.add(curr);
                }
            }

            index++;
        }

        return result;
    }

    /**
     * x - starting index in range
     * y - ending index in range
     */
    private static class IntRange extends CommonUtil.IntPair {

        // checks if strins is in form [n][-][m]
        static boolean isValid(String s) {
            Pattern pattern = Pattern.compile("(\\d*)(-?)(\\d*?)");
            Matcher matcher = pattern.matcher(s);
            return matcher.matches();
        }

        private IntRange(int x, int y) {
            super(x, y);
        }

        public IntRange(String s, int size) {
            defineFromString(s, '-', size);
        }

        public boolean isInRange(int index) {
            return index >= x && index <= y;
        }

    }

    /**
     * x - starting index
     * y - index skip - x is first, x+y second, x+2y third, end so on.
     */
    private static class IntSublist extends CommonUtil.IntPair {

        // checks if strins is in form [n][:][m]
        static boolean isValid(String s) {
            Pattern pattern = Pattern.compile("(\\d*)(:?)(\\d*?)");
            Matcher matcher = pattern.matcher(s);
            return matcher.matches();
        }

        private IntSublist(int x, int y) {
            super(x, y);
        }

        public IntSublist(String s, int size) {
            defineFromString(s, ':', size);
        }


        public boolean isInSublist(int index) {
            return (index - x) % y == 0;
        }

    }

   /**
     * Class that represents filter for list filtering. It is created based on filter string.
     * Filter string is comma separated list of filter tokens. Valid filter tokens are:
     *      m - specific integer m
     *      m-n - integers in specified range, if m is ommited it's vaue is 1, if n is
     *            ommited it's value is specified size of list to be filtered
     *      m:n - all integerers starting from m and all subsequent with step n,
     *            m, m+1*n , m+2*n, ...
     *      odd - the same as 1:2
     *      even - the same as 2:2
     *      unique - tells that list must contain unique values (no duplicates)
     */
    private static class Filter {

        private boolean isUnique = false;
        private List filterList;

        private Filter(String filterStr, int size) {
            StringTokenizer tokenizer = new StringTokenizer(filterStr, ",");
            filterList = new ArrayList();
            
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken().trim();

                if ("unique".equals(token)) {
                    isUnique = true;
                } else if ("odd".equals(token)) {
                	filterList.add(new IntSublist(1, 2));
                } else if ("even".equals(token)) {
                	filterList.add( new IntSublist(2, 2));
                } else if (IntRange.isValid(token)) {
                	filterList.add(new IntRange(token, size));
                } else if (IntSublist.isValid(token)) {
                	filterList.add(new IntSublist(token, size));
                }
            }
        }

        /**
         * Checks if specified integer passes the filter
         */
        private boolean isInFilter(int num) {
        	int listSize = filterList.size();
        	
        	if (listSize == 0) {
        		return true;
        	}
        	
            for (int i = 0; i < listSize; i++) {
            	CommonUtil.IntPair curr = (CommonUtil.IntPair) filterList.get(i);
                if ( curr instanceof IntRange && ((IntRange)curr).isInRange(num) ) {
                    return true;
                } else if ( curr instanceof IntSublist && ((IntSublist)curr).isInSublist(num) ) {
                    return true;
                }
            }

            return false;
        }

    }

}