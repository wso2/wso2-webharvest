package org.webharvest.utils;

import net.sf.saxon.om.Item;
import net.sf.saxon.trans.XPathException;
import org.webharvest.exception.ScraperXQueryException;

/**
 * @author: Vladimir Nikic
 * Date: Sep 4, 2007
 */
public class XmlNodeWrapper {

    private Item item;
    private String stringValue = null;

    public XmlNodeWrapper(Item item) {
        this.item = item;
    }

    public String toString() {
        if (stringValue == null) {
            try {
                stringValue = CommonUtil.serializeItem(this.item);
            } catch (XPathException e) {
                throw new ScraperXQueryException("Error serializing XML item!", e);

            }
        }

        return stringValue;
    }
}
