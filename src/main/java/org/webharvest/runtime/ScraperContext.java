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
package org.webharvest.runtime;

import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.Catalog;
import org.webharvest.utils.CommonUtil;
import org.webharvest.utils.SystemUtilities;

/**
 * Context of scraper execution. All the variables created during
 * scraper execution are stored in this context.
 */
public class ScraperContext extends Catalog {

    // context of the caller if context is crated in a function 
    private ScraperContext callerContext = null;

    private SystemUtilities systemUtilities;

    public ScraperContext(Scraper scraper, ScraperContext callerContext) {
		super();
        this.callerContext = callerContext;
        this.systemUtilities = new SystemUtilities(scraper);
        this.put("sys", this.systemUtilities);
        this.put("http", scraper.getHttpClientManager().getHttpInfo());
    }

    public ScraperContext(Scraper scraper) {
		this(scraper, null);
    }

	public Variable getVar(String name) {
        Variable value = (Variable) this.get(name);
        if ( value == null && callerContext != null && name.startsWith("caller.") ) {
            return callerContext.getVar(name.substring(7));
        }
        return value;
    }

    public Object setVar(Object key, Object value) {
        Variable var = CommonUtil.createVariable(value);
        return super.put(key, var);
    }

    public ScraperContext getCallerContext() {
        return callerContext;
    }

    public void dispose() {
        this.systemUtilities.setScraper(null);
    }
   
}