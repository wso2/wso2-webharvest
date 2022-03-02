package org.webharvest.runtime.processors;

import org.webharvest.definition.BaseElementDef;
import org.webharvest.definition.IElementDef;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.variables.*;

/**
 * Processor which executes only body and returns variables list.
 */
public class BodyProcessor extends BaseProcessor {

    public BodyProcessor(BaseElementDef elementDef) {
        super(elementDef);
    }

    public Variable execute(Scraper scraper, ScraperContext context) {
        IElementDef[] defs = elementDef.getOperationDefs();
        ListVariable result = new ListVariable();

        if (defs.length > 0) {
            for (int i = 0; i < defs.length; i++) {
                BaseProcessor processor = ProcessorResolver.createProcessor( defs[i], scraper.getConfiguration(), scraper );
                result.addVariable( processor.run(scraper, context) );
            }
        } else {
            result.addVariable( new NodeVariable(elementDef.getBodyText()) );
        }

        return result;
    }

}