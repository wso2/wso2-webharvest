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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;

import java.util.*;

public class XmlNode extends HashMap {

    protected static final Log log = LogFactory.getLog(XmlNode.class);

    private static final boolean IGNORE_NAMESPACE = true;
	private static final boolean IGNORE_CASE = true;

	// node name - correspondes to xml tag name
	private String name;

	// parent element
	private XmlNode parent;

	// map of attributes
	private Map attributes = new HashMap();

	// all subelements in the form of linear list
	private List elementList = new ArrayList();

	// textBuff value
	private StringBuffer textBuff = new StringBuffer();

	// text buffer containing continuous text 
	private transient StringBuffer tmpBuf = new StringBuffer();

    // location of element in the XML
    private int lineNumbwe;
    private int columnNumber;

    /**
	 * Static method that creates node for specified input source which
	 * contains XML data
	 * @param in
	 * @return XmlNode instance
	 */
	public static XmlNode getInstance(InputSource in) {
        return XmlParser.parse(in);
	}

	/**
	 * Constructor that defines name and connects to specified
	 * parent element.
	 * @param name
	 * @param parent
	 */
	protected XmlNode(String name, XmlNode parent) {
		super();

		this.name = adaptName(name);
		this.parent = parent;

		if (parent != null) {
			parent.addElement(this);
		}
	}

	/**
	 * According to settings of this object changes element/attribute
	 * names to be namespace/case insensitive.
	 * @param s
	 * @return String
	 */
	private String adaptName(String s) {
		if (IGNORE_NAMESPACE) {
			int index = s.indexOf(':');
			if (index >= 0) {
				s = s.substring(index + 1);
			}
		}

		if (IGNORE_CASE) {
			s = s.toLowerCase();
		}

		return s;
	}

	/**
	 * @return Node name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Node textBuff.
	 */
	public String getText() {
		return textBuff == null ? null : textBuff.toString();
	}

	/**
	 * @return Parent node or null if instance is root node.
	 */
	public XmlNode getParent() {
		return parent;
	}

	/**
	 * For specified serach path returns element/attribute if found,
	 * or null otherwise. Path is sequence of elements separated with
	 * some of characters: ./\[]
	 * For example: msg[0].response[0].id is trying to find in node
	 * first msg subelement and than first response subelement and then
	 * attribute id.
	 * @param key
	 * @return Resulting value which should be eather XmlNode instance or string.
	 */
	private Object getSeq(String key) {
		StringTokenizer strTkzr = new StringTokenizer(key, "./\\[]");
		Object currValue = this;
		while (strTkzr.hasMoreTokens()) {
			String currKey = strTkzr.nextToken();
			if (currValue instanceof Map) {
				currValue = ((Map)currValue).get(currKey);
			} else if (currValue instanceof List) {
				try {
					List list = (List) currValue;
					int index = Integer.parseInt(currKey);

					if (index >= 0 && index < list.size()) {
						currValue = list.get(index);
					}
				} catch (NumberFormatException e) {
					return null;
				}
			} else {
				return null;
			}
		}

		return currValue;
	}

	/**
	 * Overriden get method - search both subelements and attributes
	 */
	public Object get(Object key) {
		if (key == null) {
			return null;
		}

		if (IGNORE_CASE) {
			key = ((String)key).toLowerCase();
		}

		String sKey = (String) key;

		if ( sKey.indexOf('/') >= 0 || sKey.indexOf('.') >= 0 || sKey.indexOf('\\') >= 0 || sKey.indexOf('[') >= 0 ) {
			return getSeq(sKey);
		}

		if (sKey.equalsIgnoreCase("_value")) {
			return getText();
		} else if (this.containsKey(key)) {
			return super.get(key);
		} else {
			return attributes.get(key);
		}
	}
	
	public String getString(Object key) {
		return (String) get(key);
	}

	/**
	 * Adds new attribute with specified name and value.
	 * @param name
	 * @param value
	 */
	public void addAttribute(String name, String value) {
		attributes.put(adaptName(name), value);
	}

    public Map getAttributes() {
        return this.attributes;
    }
    
    public String getAttribute(String attName) {
    	return (String) this.attributes.get(attName);
    }

    /**
	 * Adds new subelement.
	 * @param elementNode
	 */
	public void addElement(XmlNode elementNode) {
        flushText();
        
        String elementName = elementNode.getName();

		if (!this.containsKey(elementName)) {
			this.put(elementName, new ArrayList());
		}

		ArrayList elementsForName = (ArrayList) this.get(elementName);
		elementsForName.add(elementNode);

		elementList.add(elementNode);
	}

    /**
     * Adds new textBuff to element list
     * @param value
     */
    public void addElement(String value) {
        tmpBuf.append(value);
    }

    void flushText() {
        String value = tmpBuf.toString();
        if (!"".equals(value)) {
            StringTokenizer tokenizer = new StringTokenizer(value, "\n\r");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken().trim();
                if (token.length() != 0) {
                    elementList.add(token);
                    if (textBuff.length() > 0) {
                        textBuff.append('\n');
                    }
                    textBuff.append(token);
                }
            }
            tmpBuf.delete( 0, tmpBuf.length() );
        }
    }


    public Object getElement(String name) {
        return super.get(name);
    }

    public List getElementList() {
        return elementList;
    }

    /**
	 * Prints instance in treelike form to the default output.
	 * Useful for testing.
	 */
	public void print() {
		print(0);
	}

	private void print(int level) {
		for (int i = 0; i < level; i++) {
			System.out.print("     ");
		}
		System.out.print(name + ": " + attributes + ": TEXT = [" + textBuff + "]\n");

		Iterator it = elementList.iterator();
		while (it.hasNext()) {
            Object element = it.next();
            if (element instanceof XmlNode) {
                XmlNode childNode = (XmlNode) element;
                childNode.print(level + 1);
            } else {
                for (int i = 0; i <= level; i++) {
                    System.out.print("     ");
                }
                System.out.println((String)element);
            }
		}
	}

    public void setLocation(int lineNumber, int columnNumber) {
        this.lineNumbwe = lineNumber;
        this.columnNumber = columnNumber;
    }

    public int getLineNumber() {
        return lineNumbwe;
    }

    public int getColumnNumber() {
        return columnNumber;
    }
    
}