package org.webharvest.utils;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.RuntimeConfig;
import org.webharvest.exception.ScraperXPathException;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.dom.DOMSource;
import java.io.*;

import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.Configuration;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.Item;

/**
 * XML utils - contains common logic for XML handling
 */
public class XmlUtil {

    public static void prettyPrintXml(Document doc, Writer writer) throws IOException {
        try {
            DOMSource domSource = new DOMSource(doc);
            StreamResult streamResult = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer serializer = tf.newTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            serializer.setOutputProperty(OutputKeys.METHOD, "xml");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.transform(domSource, streamResult);
        } catch (TransformerException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String prettyPrintXml(String xmlAsString) throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse( new InputSource(new StringReader(xmlAsString)) );
        StringWriter writer = new StringWriter();

        prettyPrintXml(doc, writer);

        String result = writer.toString();

        writer.close();
        return result;
    }

    /**
     * Evaluates specified XPath expression against given XML text and using given runtime configuration.
     * @param xpath
     * @param xml
     * @param runtimeConfig
     * @return Instance of ListVariable that contains results.
     * @throws XPathException
     */
    public static ListVariable evaluateXPath(String xpath, String xml, RuntimeConfig runtimeConfig) throws XPathException {
        StaticQueryContext sqc = runtimeConfig.getStaticQueryContext();
        Configuration config = sqc.getConfiguration();

        XQueryExpression exp = runtimeConfig.getXQueryExpressionPool().getCompiledExpression(xpath);
        DynamicQueryContext dynamicContext = new DynamicQueryContext(config);
        StringReader reader = new StringReader(xml);

        dynamicContext.setContextItem(sqc.buildDocument(new StreamSource(reader)));

        return createListOfXmlNodes(exp, dynamicContext);
    }

    /**
     * Creates list variable of resulting XML nodes.
     * @param exp
     * @param dynamicContext
     * @return
     * @throws XPathException
     */
    public static ListVariable createListOfXmlNodes(XQueryExpression exp, DynamicQueryContext dynamicContext) throws XPathException {
        final SequenceIterator iter = exp.iterator(dynamicContext);

        ListVariable listVariable = new ListVariable();
        while (true) {
            Item item = iter.next();
            if (item == null) {
                break;
            }

            XmlNodeWrapper value = new XmlNodeWrapper(item);
            listVariable.addVariable( new NodeVariable(value) );
        }

        return listVariable;
    }

}
