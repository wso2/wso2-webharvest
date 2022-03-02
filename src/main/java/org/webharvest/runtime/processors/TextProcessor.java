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

import org.webharvest.definition.TextDef;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.templaters.*;
import org.webharvest.runtime.variables.*;
import org.webharvest.utils.*;

/**
 * Text processor.
 */
public class TextProcessor extends BaseProcessor {

    private TextDef textDef;

    public TextProcessor(TextDef textDef) {
        super(textDef);
        this.textDef = textDef;
    }

    public Variable execute(Scraper scraper, ScraperContext context) {
        String charset = BaseTemplater.execute( textDef.getCharset(), scraper.getScriptEngine() );
        if (CommonUtil.isEmptyString(charset)) {
            charset = scraper.getConfiguration().getCharset();
        }
        String delimiter = BaseTemplater.execute( textDef.getDelimiter(), scraper.getScriptEngine() );
        if (delimiter == null) {
            delimiter = "\n";
        }

        Variable body = new BodyProcessor(textDef).execute(scraper, context);
        return new NodeVariable( body instanceof ListVariable ? ((ListVariable)body).toString(charset, delimiter) : body.toString(charset) );
    }

}