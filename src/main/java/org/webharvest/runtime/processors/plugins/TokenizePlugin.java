package org.webharvest.runtime.processors.plugins;

import org.webharvest.runtime.*;
import org.webharvest.runtime.processors.*;
import org.webharvest.runtime.variables.*;
import org.webharvest.utils.*;

/**
 * Support for database operations.
 */
public class TokenizePlugin extends WebHarvestPlugin {

    public String getName() {
        return "tokenize";
    }

    public Variable executePlugin(Scraper scraper, ScraperContext context) {
        String delimiters = evaluateAttribute("delimiters", scraper);
        if ( delimiters == null || "".equals(delimiters) ) {
            delimiters = "\n\r";
        }
        boolean trimTokens = evaluateAttributeAsBoolean("trimtokens", true, scraper);
        boolean allowWmptyTokens = evaluateAttributeAsBoolean("allowemptytokens", false, scraper);
        String text =  executeBody(scraper, context).toString();

        this.setProperty("Delimiters", delimiters);
        this.setProperty("Trim tokens", trimTokens);
        this.setProperty("Allow empty tokens", allowWmptyTokens);

        String tokens[] = CommonUtil.tokenize(text, delimiters, trimTokens, allowWmptyTokens);

        ListVariable listVariable = new ListVariable();
        for (String token: tokens) {
            listVariable.addVariable(new NodeVariable(token));
        }

        return listVariable;
    }

    public String[] getValidAttributes() {
        return new String[] {
                "delimiters",
                "trimtokens",
                "allowemptytokens"
        };
    }

    public String[] getAttributeValueSuggestions(String attributeName) {
        if ("trimtokens".equalsIgnoreCase(attributeName)) {
            return new String[] {"true", "false"};
        } else if ("allowemptytokens".equalsIgnoreCase(attributeName)) {
            return new String[] {"true", "false"};
        }
        return null;
    }

}