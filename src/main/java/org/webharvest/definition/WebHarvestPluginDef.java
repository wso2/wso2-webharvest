package org.webharvest.definition;

import org.webharvest.runtime.processors.*;
import org.webharvest.exception.PluginException;

import java.util.*;

/**
 * Definition of all plugin processors.
 */
public class WebHarvestPluginDef extends BaseElementDef {

	private Map attributes;
    private Class pluginClass;
    private String name;

    public WebHarvestPluginDef(XmlNode xmlNode) {
        super(xmlNode, true);
        this.attributes = xmlNode.getAttributes();
    }

    void setPluginClass(Class pluginClass) {
        this.pluginClass = pluginClass;
    }

    void setPluginName(String name) {
        this.name = name;
    }

    public Map getAttributes() {
        return attributes;
    }

    public WebHarvestPlugin createPlugin() {
        if (pluginClass != null) {
            try {
                WebHarvestPlugin plugin = (WebHarvestPlugin) pluginClass.newInstance();
                plugin.setDef(this);
                return plugin;
            } catch (Exception e) {
                throw new PluginException(e);
            }
        }

        throw new PluginException("Cannot create plugin!");
    }

    public String getShortElementName() {
        return name != null ? name.toLowerCase() : "unknown plugin";
    }
    
}