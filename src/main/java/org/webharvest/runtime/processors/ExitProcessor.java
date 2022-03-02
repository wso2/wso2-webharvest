package org.webharvest.runtime.processors;

import org.webharvest.definition.ExitDef;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.utils.CommonUtil;

/**
 * Exit processor.
 */
public class ExitProcessor extends BaseProcessor {

    private ExitDef exitDef;

    public ExitProcessor(ExitDef exitDef) {
        super(exitDef);
        this.exitDef = exitDef;
    }

    public Variable execute(Scraper scraper, ScraperContext context) {
        String condition = BaseTemplater.execute( exitDef.getCondition(), scraper.getScriptEngine());
        if ( condition == null || "".equals(condition) ) {
            condition = "true";
        }

        if (CommonUtil.isBooleanTrue(condition)) {
            String message = BaseTemplater.execute( exitDef.getMessage(), scraper.getScriptEngine());
            if (message == null) {
                message = "";
            }
            scraper.exitExecution(message);
            if ( scraper.getLogger().isInfoEnabled() ) {
                scraper.getLogger().info("Configuration exited: " + message);
            }
        }

        return new EmptyVariable();
    }

}
