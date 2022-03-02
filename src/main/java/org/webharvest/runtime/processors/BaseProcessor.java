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
package org.webharvest.runtime.processors;

import org.webharvest.definition.*;
import org.webharvest.runtime.*;
import org.webharvest.runtime.templaters.*;
import org.webharvest.runtime.variables.*;
import org.webharvest.utils.*;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Base processor that contains common processor logic.
 * All other processors extend this class.
 */
abstract public class BaseProcessor {

    abstract public Variable execute(Scraper scraper, ScraperContext context);

    protected BaseElementDef elementDef;
    private Map properties = new LinkedHashMap();

    protected BaseProcessor() {
    }

    /**
     * Base constructor - assigns element definition to the processor.
     * @param elementDef
     */
    protected BaseProcessor(BaseElementDef elementDef) {
        this.elementDef = elementDef;
    }

    /**
     * Wrapper for the execute method. Adds controling and logging logic.
     */
    public Variable run(Scraper scraper, ScraperContext context) {
        int scraperStatus = scraper.getStatus();

        if (scraperStatus == Scraper.STATUS_STOPPED || scraperStatus == Scraper.STATUS_EXIT) {
            return EmptyVariable.INSTANCE;
        }

        if (scraperStatus == Scraper.STATUS_PAUSED) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS");
            try {
                synchronized(scraper) {
                    if ( scraper.getLogger().isInfoEnabled() ) {
                        scraper.getLogger().info("Execution paused [" + dateFormatter.format(new Date()) + "].");
                    }
                    scraper.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            scraper.continueExecution();
            if ( scraper.getLogger().isInfoEnabled() ) {
                scraper.getLogger().info("Execution continued [" + dateFormatter.format(new Date()) + "].");
            }
        }

        long startTime = System.currentTimeMillis();

        int runningLevel = scraper.getRunningLevel();

        String id = (this.elementDef != null) ? BaseTemplater.execute( this.elementDef.getId(), scraper.getScriptEngine() ) : null;
        String idDesc = id != null ? "[ID=" + id + "] " : "";
        String indent = CommonUtil.replicate("    ", runningLevel-1);

        setProperty("ID", id);

        if ( scraper.getLogger().isInfoEnabled() ) {
            scraper.getLogger().info(indent + CommonUtil.getClassName(this) + " starts processing..." + idDesc);
        }

        scraper.setExecutingProcessor(this);
        Variable result = execute(scraper, context);
        long executionTime = System.currentTimeMillis() - startTime;

        setProperty(Constants.EXECUTION_TIME_PROPERTY_NAME, new Long(executionTime));
        setProperty(Constants.VALUE_PROPERTY_NAME, result);

        scraper.processorFinishedExecution(this, this.properties);
        scraper.finishExecutingProcessor();

        // if debug mode is true and processor ID is not null then write debugging file
        if (scraper.isDebugMode() && id != null) {
            writeDebugFile(result, id, scraper);
        }

        if ( scraper.getLogger().isInfoEnabled() ) {
            scraper.getLogger().info(indent + CommonUtil.getClassName(this) + " processor executed in " + executionTime + "ms." + idDesc);
        }

        return result;
    }

    /**
     * Defines processor runtime property with specified name and value.
     * @param name
     * @param value
     */
    protected void setProperty(String name, Object value) {
        if ( name != null && !"".equals(name) && value != null ) {
            this.properties.put(name, value);
        }
    }

    protected void debug(BaseElementDef elementDef, Scraper scraper, Variable variable) {
        String id = (elementDef != null) ? BaseTemplater.execute( elementDef.getId(), scraper.getScriptEngine() ) : null;

        if (scraper.isDebugMode() && id != null) {
            if (variable != null) {
                writeDebugFile(variable, id, scraper);
            }
        }
    }

    protected Variable getBodyTextContent(BaseElementDef elementDef, Scraper scraper, ScraperContext context,
                                           boolean registerExecution, KeyValuePair properties[]) {
        if (elementDef == null) {
            return null;
        } else if (elementDef.hasOperations()) {
            BodyProcessor bodyProcessor = new BodyProcessor(elementDef);
            if (properties != null) {
                for (int i = 0; i < properties.length; i++) {
                    bodyProcessor.setProperty(properties[i].getKey(), properties[i].getValue());
                }
            }
            Variable body = registerExecution ?  bodyProcessor.run(scraper, context) :  bodyProcessor.execute(scraper, context);
            return new NodeVariable( body == null ? "" : body.toString() );
        } else {
            return new NodeVariable(elementDef.getBodyText());
        }
    }

    protected Variable getBodyTextContent(BaseElementDef elementDef, Scraper scraper, ScraperContext context, boolean registerExecution) {
        return getBodyTextContent(elementDef, scraper, context, registerExecution, null);
    }

    protected Variable getBodyTextContent(BaseElementDef elementDef, Scraper scraper, ScraperContext context) {
        return getBodyTextContent(elementDef, scraper, context, false);
    }

    protected BaseProcessor[] getSubprocessors(Scraper scraper) {
        IElementDef[] defs = elementDef.getOperationDefs();
        BaseProcessor result[] = new BaseProcessor[defs.length];

        for (int i = 0; i < defs.length; i++) {
            result[i] = ProcessorResolver.createProcessor( defs[i], scraper.getConfiguration(), scraper );
        }

        return result;
    }

    public BaseElementDef getElementDef() {
        return elementDef;
    }

    private void writeDebugFile(Variable var, String processorId, Scraper scraper) {
        byte[] data = var == null ? new byte[] {} : var.toString().getBytes();

        String workingDir = scraper.getWorkingDir();
        String dir = CommonUtil.getAbsoluteFilename(workingDir, "_debug");

        int index = 1;
        String fullPath = dir + "/" + processorId + "_" + index + ".debug";
        while ( new File(fullPath).exists() ) {
            index++;
            fullPath = dir + "/" + processorId + "_" + index + ".debug";
        }

        FileOutputStream out;
        try {
            new File(dir).mkdirs();
            out = new FileOutputStream(fullPath, false);
            out.write(data);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}