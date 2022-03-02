package org.webharvest.definition;

import org.webharvest.exception.ConfigurationException;
import org.webharvest.exception.ErrMsg;
import org.webharvest.runtime.processors.*;
import org.webharvest.utils.*;
import org.webharvest.gui.*;

import java.util.*;
import java.nio.charset.*;

/**
 * @author: Vladimir Nikic
 * Date: May 24, 2007
 */
public class ElementInfo {

    // properties containing suggested attribute values
    private static final Properties attrValuesProperties = ResourceManager.getAttrValuesProperties();

    private String name;
    private Class pluginClass;
    private boolean isInternalPlugin;
    private Class definitionClass;
    private String validTags;
    private String validAtts;

    private Set tagsSet = new TreeSet();
    private Set requiredTagsSet = new TreeSet();
    private Set attsSet = new TreeSet();
    private Set requiredAttsSet = new TreeSet();

    private boolean allTagsAllowed;

    // pluging instance for this element, if element represents Web-Harvest plugin
    private WebHarvestPlugin plugin = null;

    public ElementInfo(String name, Class definitionClass, String validTags, String validAtts) {
        this(name, null, true, definitionClass, validTags, validAtts);
    }

    public ElementInfo(String name, Class pluginClass, boolean isInternalPlugin, Class definitionClass, String validTags, String validAtts) {
        this.name = name;
        this.pluginClass = pluginClass;
        this.isInternalPlugin = isInternalPlugin;
        this.definitionClass = definitionClass;
        this.validTags = validTags;
        this.validAtts = validAtts;

        this.allTagsAllowed = validTags == null;
        
        if (validTags != null) {
            StringTokenizer tokenizer = new StringTokenizer(validTags, ",");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken().toLowerCase();
                if ( token.startsWith("!") ) {
                    token = token.substring(1);
                    this.requiredTagsSet.add(token);
                }
                this.tagsSet.add(token);
            }
        }

        if (validAtts != null) {
            StringTokenizer tokenizer = new StringTokenizer(validAtts, ",");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken().toLowerCase();
                if ( token.startsWith("!") ) {
                    token = token.substring(1);
                    this.requiredAttsSet.add(token);
                }
                this.attsSet.add(token);
            }
        }
    }

    /**
     * @param onlyRequiredAtts
     * @return Template with allowed attributes. 
     */
    public String getTemplate(boolean onlyRequiredAtts) {
        StringBuffer result = new StringBuffer("<" + this.name);
        
        Set atts = onlyRequiredAtts ? this.requiredAttsSet : this.attsSet;

        Iterator iterator = atts.iterator();
        while (iterator.hasNext()) {
            String att = (String) iterator.next();
            result.append(" " + att + "=\"\"");
        }

        // if no valid subtags
        if ( this.validTags != null && "".equals(this.validTags.trim()) ) {
            result.append("/>");
        } else {
            result.append("></" + name + ">");
        }

        return result.toString();
    }


    public Class getPluginClass() {
        return pluginClass;
    }

    public boolean isInternalPlugin() {
        return isInternalPlugin;
    }

    public Class getDefinitionClass() {
        return definitionClass;
    }

    public String getName() {
        return name;
    }

    public Set getTagsSet() {
        return tagsSet;
    }

    public Set getAttsSet() {
        return attsSet;
    }

    public Set getRequiredAttsSet() {
        return requiredAttsSet;
    }

    public Set getRequiredTagsSet() {
        return requiredTagsSet;
    }

    public boolean areAllTagsAllowed() {
        return allTagsAllowed;
    }

    /**
     * @return Array of suggested values for specified attribute - used for auto-completion in IDE.
     */
    public String[] getAttributeValueSuggestions(String attributeName) {
        if (plugin != null) {
            return plugin.getAttributeValueSuggestions(attributeName);
        } else {
            if (attrValuesProperties != null && attributeName != null) {
                String key = name.toLowerCase() + "." + attributeName.toLowerCase();
                String values = attrValuesProperties.getProperty(key);
                if ("*charset".equalsIgnoreCase(values)) {
                    Set<String> charsetKeys = Charset.availableCharsets().keySet();
                    return new ArrayList<String>(charsetKeys).toArray(new String[charsetKeys.size()]);
                } else if ("*mime".equalsIgnoreCase(values)) {
                    return ResourceManager.MIME_TYPES;
                } else {
                    return CommonUtil.tokenize(values, ",");
                }
            }
        }
        
        return null;
    }

    public void setPlugin(WebHarvestPlugin plugin) {
        this.plugin = plugin;
    }

}