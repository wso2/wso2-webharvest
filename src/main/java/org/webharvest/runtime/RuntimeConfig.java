package org.webharvest.runtime;

import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.Configuration;
import org.webharvest.runtime.processors.XQueryExpressionPool;

/**
 * Facade for runtime objects needed for specific processors' execution.
 * 
 * @author: Vladimir Nikic
 * Date: Jul 4, 2007
 */
public class RuntimeConfig {

    private StaticQueryContext staticQueryContext;

    private XQueryExpressionPool xQueryExpressionPool;

    public synchronized StaticQueryContext getStaticQueryContext() {
        if (staticQueryContext == null) {
            Configuration config = new Configuration();
            staticQueryContext = new StaticQueryContext(config);
        }
        return staticQueryContext;
    }

    public synchronized XQueryExpressionPool getXQueryExpressionPool() {
        if (xQueryExpressionPool == null) {
            xQueryExpressionPool = new XQueryExpressionPool(getStaticQueryContext());
        }

        return xQueryExpressionPool;
    }

}