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
 * Converter from XML to JSON
 */
public class XmlToJsonPlugin extends WebHarvestPlugin {

    public String getName() {
        return "xml-to-json";
    }

    public Variable executePlugin(Scraper scraper, ScraperContext context) {
        Variable body = executeBody(scraper, context);
        try {
            JSONObject jsonObject = XML.toJSONObject(body.toString());
            return new NodeVariable(jsonObject.toString());
        } catch (JSONException e) {
            throw new PluginException("Error converting XML to JSON: " + e.getMessage());
        }
    }

}