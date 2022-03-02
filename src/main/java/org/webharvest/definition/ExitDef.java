package org.webharvest.definition;

/**
 * Definition of exit processor.
 */
public class ExitDef extends BaseElementDef {

	private String condition;
	private String message;

    public ExitDef(XmlNode xmlNode) {
        super(xmlNode, false);

        this.condition = xmlNode.getAttribute("condition");
        this.message = xmlNode.getAttribute("message");
    }

    public String getCondition() {
		return condition;
	}

    public String getMessage() {
        return message;
    }

    public String getShortElementName() {
        return "exit";
    }

}