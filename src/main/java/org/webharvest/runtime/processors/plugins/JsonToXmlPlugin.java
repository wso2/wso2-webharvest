package org.webharvest.runtime.processors.plugins;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.webharvest.exception.PluginException;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;

/**
 * Converter from JSON to XML
 */
public class JsonToXmlPlugin extends WebHarvestPlugin {

    public String getName() {
        return "json-to-xml";
    }

    public Variable executePlugin(Scraper scraper, ScraperContext context) {
        Variable body = executeBody(scraper, context);
        try {
            JSONObject jsonObject = new JSONObject(body.toString());
            return new NodeVariable( XML.toString(jsonObject) );
        } catch (JSONException e) {
            throw new PluginException("Error converting JSON to XML: " + e.getMessage());
        }
    }

}