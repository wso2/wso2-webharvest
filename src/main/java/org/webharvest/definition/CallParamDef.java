package org.webharvest.definition;

/**
 * Definition of function call parameter.
 */
public class CallParamDef extends BaseElementDef {

    private String name;

    public CallParamDef(XmlNode xmlNode) {
    	super(xmlNode);
        this.name = xmlNode.getAttribute("name");
    }

    public String getName() {
    	return name;
    }

    public String getShortElementName() {
        return "call-param";
    }

}