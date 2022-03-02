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
package org.webharvest.runtime.templaters;

import org.webharvest.runtime.scripting.ScriptEngine;

/**
 * Simple templater - replaces ${expression} sequences in string with evaluated expressions.
 * Specified script engine is used for evaluation.
 */
public class BaseTemplater {
	
	public static String VAR_START = "${";
	public static String VAR_END = "}";

    public static String execute(String source, ScriptEngine scriptEngine) {
        if (source == null) {
            return source;
        }
        
        String result = "";
        
        int startIndex = source.indexOf(VAR_START);
        int endIndex = -1;
        
        while (startIndex >= 0 && startIndex < source.length()) {
        	result += source.substring(endIndex + 1, startIndex);
        	endIndex = source.indexOf(VAR_END, startIndex);
        	
        	if (endIndex > startIndex) {
        		String expression = source.substring(startIndex + VAR_START.length(), endIndex);
                Object resultObj = scriptEngine.eval(expression);
                result += resultObj == null ? "" : resultObj.toString();
        	}
        	
        	startIndex = source.indexOf( VAR_START, Math.max(endIndex + VAR_END.length(), startIndex + 1) );
        }
        
        result += source.substring(endIndex + 1);

        return result;
    }

}