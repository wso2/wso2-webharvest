package org.webharvest.runtime.processors;

import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;

import java.util.Map;
import java.util.Hashtable;

/**
 * Class represnts simple pool for XQuery expressions.
 * Client obtains compiled query using method getCompiledExpression.
 *
 * @author: Vladimir Nikic
 * Date: Jul 4, 2007
 */
public class XQueryExpressionPool {

    private StaticQueryContext sqc;
    private Map pool = new Hashtable();

    public XQueryExpressionPool(StaticQueryContext sqc) {
        this.sqc = sqc;
    }

    public synchronized XQueryExpression getCompiledExpression(String query) throws XPathException {
        if ( pool.containsKey(query) ) {
            return (XQueryExpression) pool.get(query);
        } else {
            XQueryExpression exp = sqc.compileQuery(query);
            pool.put(query, exp);
            return exp;
        }
    }

}