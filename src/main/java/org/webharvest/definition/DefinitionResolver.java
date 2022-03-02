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
package org.webharvest.definition;

import org.webharvest.exception.*;
import org.webharvest.runtime.processors.*;
import org.webharvest.utils.*;

import java.util.*;
import java.lang.reflect.Constructor;

import org.webharvest.runtime.processors.plugins.*;

/**
 * Class contains information and logic to validate and crate definition classes for
 * parsed xml nodes from Web-Harvest configurations.
 * 
 * @author Vladimir Nikic
 */
public class DefinitionResolver {

    private static Map<String, ElementInfo> elementInfos = new TreeMap<String, ElementInfo>();

    // map containing pairs (class name, plugin name) of externally registered plugins
    private static Map<String, String> externalPlugins = new LinkedHashMap<String, String>();

    // map of external plugin dependances
    private static Map<String, Class[]> externalPluginDependaces = new HashMap<String, Class[]>(); 

    // defines all valid elements of Web-Harvest configuration file
    static {
        String htmlToXmlAtts = "id,outputtype,advancedxmlescape,usecdata,specialentities,unicodechars," +
                               "omitunknowntags,treatunknowntagsascontent,omitdeprtags,treatdeprtagsascontent," +
                               "omitxmldecl,omitcomments,omithtmlenvelope,useemptyelementtags,allowmultiwordattributes," +
                               "allowhtmlinsideattributes,namespacesaware,hyphenreplacement,prunetags,booleanatts";
        elementInfos.put( "config", new ElementInfo("config", BaseElementDef.class, null, "charset,scriptlang,id") );
        elementInfos.put( "empty", new ElementInfo("empty", EmptyDef.class, null, "id") );
        elementInfos.put( "text", new ElementInfo("text", TextDef.class, null, "id,charset,delimiter") );
        elementInfos.put( "file", new ElementInfo("file", FileDef.class, null, "id,!path,action,type,charset,listfilter,listfiles,listdirs,listrecursive") );
        elementInfos.put( "var-def", new ElementInfo("var-def", VarDefDef.class, null, "id,!name,overwrite") );
        elementInfos.put( "var", new ElementInfo("var", VarDef.class, "", "id,!name") );
        elementInfos.put( "http", new ElementInfo("http", HttpDef.class, null, "id,!url,method,multipart,charset,username,password,cookie-policy") );
        elementInfos.put( "http-param", new ElementInfo("http-param", HttpParamDef.class, null, "id,!name,isfile,filename,contenttype") );
        elementInfos.put( "http-header", new ElementInfo("http-header", HttpHeaderDef.class, null, "id,!name") );
        elementInfos.put( "html-to-xml", new ElementInfo("html-to-xml", HtmlToXmlDef.class, null, htmlToXmlAtts) );
        elementInfos.put( "regexp", new ElementInfo("regexp", RegexpDef.class, "!regexp-pattern,!regexp-source,regexp-result", "id,replace,max,flag-caseinsensitive,flag-multiline,flag-dotall,flag-unicodecase,flag-canoneq") );
        elementInfos.put( "regexp-pattern", new ElementInfo("regexp-pattern", BaseElementDef.class, null, "id") );
        elementInfos.put( "regexp-source", new ElementInfo("regexp-source", BaseElementDef.class, null, "id") );
        elementInfos.put( "regexp-result", new ElementInfo("regexp-result", BaseElementDef.class, null, "id") );
        elementInfos.put( "xpath", new ElementInfo("xpath", XPathDef.class, null, "id,!expression") );
        elementInfos.put( "xquery", new ElementInfo("xquery", XQueryDef.class, "xq-param,!xq-expression", "id") );
        elementInfos.put( "xq-param", new ElementInfo("xq-param", BaseElementDef.class, null, "!name,type,id") );
        elementInfos.put( "xq-expression", new ElementInfo("xq-expression", BaseElementDef.class, null, "id") );
        elementInfos.put( "xslt", new ElementInfo("xslt", XsltDef.class, "!xml,!stylesheet", "id") );
        elementInfos.put( "xml", new ElementInfo("xml", BaseElementDef.class, null, "id") );
        elementInfos.put( "stylesheet", new ElementInfo("stylesheet", BaseElementDef.class, null, "id") );
        elementInfos.put( "template", new ElementInfo("template", TemplateDef.class, null, "id,language") );
        elementInfos.put( "case", new ElementInfo("case", CaseDef.class, "!if,else", "id") );
        elementInfos.put( "if", new ElementInfo("if", BaseElementDef.class, null, "!condition,id") );
        elementInfos.put( "else", new ElementInfo("else", BaseElementDef.class, null, "id") );
        elementInfos.put( "loop", new ElementInfo("loop", LoopDef.class, "!list,!body", "id,item,index,maxloops,filter,empty") );
        elementInfos.put( "list", new ElementInfo("list", BaseElementDef.class, null, "id") );
        elementInfos.put( "body", new ElementInfo("body", BaseElementDef.class, null, "id") );
        elementInfos.put( "while", new ElementInfo("while", WhileDef.class, null, "id,!condition,index,maxloops,empty") );
        elementInfos.put( "function", new ElementInfo("function", FunctionDef.class, null, "id,!name") );
        elementInfos.put( "return", new ElementInfo("return", ReturnDef.class, null, "id") );
        elementInfos.put( "call", new ElementInfo("call", CallDef.class, null, "id,!name") );
        elementInfos.put( "call-param", new ElementInfo("call-param", CallParamDef.class, null, "id,!name") );
        elementInfos.put( "include", new ElementInfo("include", IncludeDef.class, "", "id,!path") );
        elementInfos.put( "try", new ElementInfo("try", TryDef.class, "!body,!catch", "id") );
        elementInfos.put( "catch", new ElementInfo("catch", BaseElementDef.class, null, "id") );
        elementInfos.put( "script", new ElementInfo("script", ScriptDef.class, null, "id,language,return") );
        elementInfos.put( "exit", new ElementInfo("exit", ExitDef.class, "", "id,condition,message") );

        registerPlugin(DatabasePlugin.class, true);
        registerPlugin(JsonToXmlPlugin.class, true);
        registerPlugin(XmlToJsonPlugin.class, true);
        registerPlugin(MailPlugin.class, true);
        registerPlugin(ZipPlugin.class, true);
        registerPlugin(FtpPlugin.class, true);
        registerPlugin(TokenizePlugin.class, true);
    }

    private static void registerPlugin(Class pluginClass, boolean isInternalPlugin) {
        String fullClassName = pluginClass != null ? pluginClass.getName() : "null";
        try {
            Object pluginObj = pluginClass.newInstance();
            if ( !(pluginObj instanceof WebHarvestPlugin) ) {
                throw new PluginException("Plugin class \"" + fullClassName + "\" does not extend WebHarvestPlugin class!");
            }
            WebHarvestPlugin plugin = (WebHarvestPlugin) pluginObj;
            String pluginName = plugin.getName();
            if ( !CommonUtil.isValidXmlIdentifier(pluginName) ) {
                throw new PluginException("Plugin class \"" + fullClassName + "\" does not define valid name!");
            }
            pluginName = pluginName.toLowerCase();

            if (elementInfos.containsKey(pluginName)) {
                throw new PluginException("Plugin named \"" + pluginName + "\" is already registered!");
            }

            String subtags = plugin.getTagDesc();
            String atts = plugin.getAttributeDesc();
            ElementInfo elementInfo = new ElementInfo(pluginName, pluginClass, isInternalPlugin, WebHarvestPluginDef.class, subtags, atts);
            elementInfo.setPlugin(plugin);
            elementInfos.put(pluginName, elementInfo);
            if (!isInternalPlugin) {
                externalPlugins.put(pluginClass.getName(), pluginName);
            }
            externalPluginDependaces.put(pluginName, plugin.getDependantProcessors());

            Class[] subprocessorClasses = plugin.getDependantProcessors();
            if (subprocessorClasses != null) {
                for (Class subClass: subprocessorClasses) {
                    registerPlugin(subClass, isInternalPlugin);
                }
            }
        } catch (Exception e) {
            throw new PluginException("Error instantiating plugin class \"" + fullClassName + "\": " + e.getMessage(), e);
        }
    }

    public static void registerPlugin(Class pluginClass) throws PluginException {
        registerPlugin(pluginClass, false);
    }
    
    public static void registerPlugin(String fullClassName) throws PluginException {
        Class pluginClass = ClassLoaderUtil.getPluginClass(fullClassName);
        registerPlugin(pluginClass, false);
    }

    public static void unregisterPlugin(Class pluginClass) {
        if (pluginClass != null) {
            unregisterPlugin(pluginClass.getName());
        }
    }

    public static void unregisterPlugin(String className) {
        // only external plugins can be unregistered
        if ( isPluginRegistered(className)) {
            String pluginName = externalPlugins.get(className);
            elementInfos.remove(pluginName);
            externalPlugins.remove(className);

            // unregister deependant classes as well
            Class[] dependantClasses = externalPluginDependaces.get(pluginName);
            externalPluginDependaces.remove(pluginName);
            if (dependantClasses != null) {
                for (Class c: dependantClasses) {
                    unregisterPlugin(c);
                }
            }
        }
    }

    public static boolean isPluginRegistered(String className) {
        return externalPlugins.containsKey(className);
    }

    public static boolean isPluginRegistered(Class pluginClass) {
        return pluginClass != null && isPluginRegistered(pluginClass.getName());
    }

    public static Map getExternalPlugins() {
        return externalPlugins;
    }

    /**
     * @return Map of all allowed element infos.
     */
    public static Map getElementInfos() {
        return elementInfos;
    }

    /**
     * @param name
     * @return Instance of ElementInfo class for the specified element name,
     *         or null if no element is defined. 
     */
    public static ElementInfo getElementInfo(String name) {
        return elementInfos.get(name);
    }

    /**
     * Creates proper element definition instance based on given xml node
     * from input configuration.
     * @param node
     * @return Instance of IElementDef, or exception is thrown if cannot find
     *         appropriate element definition.
     */
    public static IElementDef createElementDefinition(XmlNode node) {
    	String nodeName = node.getName();

        ElementInfo elementInfo = getElementInfo(nodeName);
        if (elementInfo == null || elementInfo.getDefinitionClass() == null || elementInfo.getDefinitionClass() == BaseElementDef.class) {
            throw new ConfigurationException("Unexpected configuration element: " + nodeName + "!");
        }

        validate(node);

        Class elementClass = elementInfo.getDefinitionClass();
        try {
            Constructor constructor = elementClass.getConstructor( new Class[] {XmlNode.class} );
            IElementDef elementDef = (IElementDef) constructor.newInstance(new Object[]{node});
            if (elementDef instanceof WebHarvestPluginDef) {
                WebHarvestPluginDef pluginDef = (WebHarvestPluginDef) elementDef;
                pluginDef.setPluginClass( elementInfo.getPluginClass() );
                pluginDef.setPluginName( elementInfo.getName() );
            }
            return elementDef;
        } catch (Exception e) {
            if (e instanceof ConfigurationException) {
                throw (ConfigurationException) e;
            } else if (e.getCause() instanceof ConfigurationException) {
                throw (ConfigurationException) e.getCause();
            }
            throw new ConfigurationException("Cannot create class instance: " + elementClass + "!");
        }
    }

    /**
     * Validates specified xml node with appropriate element info instance.
     * If validation fails, an runtime exception is thrown.
     * @param node
     */
    public static void validate(XmlNode node) {
        if (node == null) {
            return;
        }

        String nodeName = node.getName().toLowerCase();
        ElementInfo elementInfo = getElementInfo(nodeName);

        if (elementInfo == null) {
            return;
        }
        
        Set tags = elementInfo.getTagsSet();
        Set requiredTags = elementInfo.getRequiredTagsSet();
        boolean areAllTagsAllowed = elementInfo.areAllTagsAllowed();
        Set allTagNameSet = elementInfos.keySet();

        // checks if tag contains all required subelements
        Iterator requiredTagsIterator = requiredTags.iterator();
        while (requiredTagsIterator.hasNext()) {
            String tag = (String) requiredTagsIterator.next();
            if ( node.getElement(tag) == null ) {
                throw new ConfigurationException( ErrMsg.missingTag(node.getName(), tag) );
            }
        }

        // check if element contains only allowed subelements
        Iterator subtagsIterator = node.keySet().iterator();
        while (subtagsIterator.hasNext()) {
            String tagName = ((String) subtagsIterator.next()).toLowerCase();
            if ( (!areAllTagsAllowed && !tags.contains(tagName)) || (areAllTagsAllowed && !allTagNameSet.contains(tagName)) ) {
                throw new ConfigurationException( ErrMsg.invalidTag(node.getName(), tagName) );
            }
        }

        Set atts = elementInfo.getAttsSet();
        Set requiredAtts = elementInfo.getRequiredAttsSet();

        // checks if tag contains all required subelements
        Iterator requiredAttsIterator = requiredAtts.iterator();
        while (requiredAttsIterator.hasNext()) {
            String att = (String) requiredAttsIterator.next();
            if ( node.getAttribute(att) == null ) {
                throw new ConfigurationException( ErrMsg.missingAttribute(node.getName(), att) );
            }
        }

        // check if element contains only allowed attributes
        Map attributes = node.getAttributes();
        if (attributes != null) {
            Iterator it = attributes.keySet().iterator();
            while (it.hasNext()) {
                String attName = ((String) it.next()).toLowerCase();
                if ( !atts.contains(attName) ) {
                    throw new ConfigurationException( ErrMsg.invalidAttribute(node.getName(), attName) );
                }
            }
        }
    }

}