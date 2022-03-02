package org.webharvest.utils;

import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.StringReader;

/**
 * @author: Vladimir Nikic
 * Date: May 9, 2007
 */
public class XmlValidator extends DefaultHandler {
    
    int lineNumber, columnNumber;
    private Exception exception;

    public boolean parse(InputSource in) {
        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setValidating(false);
            parserFactory.setNamespaceAware(false);

            SAXParser parser = parserFactory.newSAXParser();

            // call parsing
            parser.parse(in, this);

            this.exception = null;
            this.lineNumber = 0;
            this.columnNumber = 0;

            return true;
        } catch (Exception e) {
            this.exception = e;
            if (e instanceof SAXParseException) {
                SAXParseException saxException = (SAXParseException) e;
                this.lineNumber = saxException.getLineNumber();
                this.columnNumber = saxException.getColumnNumber();
            }
            return false;
        }
    }

    public Exception getException() {
        return exception;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }
    
}