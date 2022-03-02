package org.webharvest.runtime.processors.plugins;

import org.webharvest.exception.*;
import org.webharvest.runtime.*;
import org.webharvest.runtime.processors.*;
import org.webharvest.runtime.variables.*;
import org.webharvest.utils.*;

/**
 * DB param plugin - can be used only inside database plugin.
 */
public class DbParamPlugin extends WebHarvestPlugin {

    public String getName() {
        return "db-param";
    }

    public Variable executePlugin(Scraper scraper, ScraperContext context) {
        BaseProcessor processor = scraper.getRunningProcessorOfType(DatabasePlugin.class);
        if (processor != null) {
            DatabasePlugin databasePlugin = (DatabasePlugin) processor;
            String type = evaluateAttribute("type", scraper);
            Variable body = executeBody(scraper, context);
            if (CommonUtil.isEmptyString(type)) {
                type = "text";
                if ( body.getWrappedObject() instanceof byte[] ) {
                    type = "binary";
                } else if (body instanceof ListVariable) {
                    ListVariable list = (ListVariable) body;
                    if (list.toList().size() == 1 && list.get(0).getWrappedObject() instanceof byte[]) {
                        type = "binary";
                    }
                }
            }
            databasePlugin.addDbParam(body, type);
            return new NodeVariable("?");
        } else {
            throw new PluginException("Cannot use db-param attach plugin out of database plugin context!");
        }
    }

    public String[] getValidAttributes() {
        return new String[] {"type"};
    }

    public String[] getAttributeValueSuggestions(String attributeName) {
        if ("type".equalsIgnoreCase(attributeName)) {
            return new String[] {"int", "long", "double", "text", "binary"};
        }
        return null;
    }


}