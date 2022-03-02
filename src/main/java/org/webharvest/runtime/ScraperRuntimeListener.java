package org.webharvest.runtime;

import org.webharvest.runtime.processors.BaseProcessor;

import java.util.Map;

/**
 * @author: Vladimir Nikic
 * Date: Apr 20, 2007
 */
public interface ScraperRuntimeListener {

    public void onExecutionStart(Scraper scraper);

    public void onExecutionPaused(Scraper scraper);

    public void onExecutionContinued(Scraper scraper);

    public void onNewProcessorExecution(Scraper scraper, BaseProcessor processor);
    
    public void onExecutionEnd(Scraper scraper);

    public void onProcessorExecutionFinished(Scraper scraper, BaseProcessor processor, Map properties);

    public void onExecutionError(Scraper scraper, Exception e);
    
}