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
package org.webharvest.runtime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.webharvest.definition.IElementDef;
import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.runtime.processors.BaseProcessor;
import org.webharvest.runtime.processors.CallProcessor;
import org.webharvest.runtime.processors.HttpProcessor;
import org.webharvest.runtime.processors.ProcessorResolver;
import org.webharvest.runtime.scripting.ScriptEngine;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.web.HttpClientManager;
import org.webharvest.utils.CommonUtil;
import org.webharvest.utils.Stack;
import org.webharvest.utils.ClassLoaderUtil;
import org.webharvest.exception.DatabaseException;

import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Basic runtime class.
 */
public class Scraper {

    public static final int STATUS_READY = 0;
    public static final int STATUS_RUNNING = 1;
    public static final int STATUS_PAUSED = 2;
    public static final int STATUS_FINISHED = 3;
    public static final int STATUS_STOPPED = 4;
    public static final int STATUS_ERROR = 5;
    public static final int STATUS_EXIT = 6;

    private Log logger = LogFactory.getLog("" + System.currentTimeMillis());
    private ScraperConfiguration configuration;
    private String workingDir;
    private ScraperContext context;

    private RuntimeConfig runtimeConfig;

    private transient boolean isDebugMode = false;

    private HttpClientManager httpClientManager;

    // stack of running processors
    private transient Stack runningProcessors = new Stack();

    // stack of running functions
    private transient Stack runningFunctions = new Stack();

    // params that are proceeded to calling function
    private transient Map functionParams = new HashMap();

    // stack of running http processors
    private transient Stack runningHttpProcessors = new Stack();

    // default script engine used throughout the configuration execution
    private ScriptEngine scriptEngine = null;

    // all used script engines in this scraper
    private Map usedScriptEngines = new HashMap();

    // pool of used database connections
    Map dbPool = new HashMap();

    private List<ScraperRuntimeListener> scraperRuntimeListeners = new LinkedList<ScraperRuntimeListener>();

    private int status = STATUS_READY;

    private String message = null;

    /**
     * Constructor.
     * @param configuration
     * @param workingDir
     */
    public Scraper(ScraperConfiguration configuration, String workingDir) {
        this.configuration = configuration;
        this.runtimeConfig = new RuntimeConfig();
        this.workingDir = CommonUtil.adaptFilename(workingDir);

        this.httpClientManager = new HttpClientManager();

        this.context = new ScraperContext(this);
        this.scriptEngine = configuration.createScriptEngine(this.context);
        this.usedScriptEngines.put(configuration.getDefaultScriptEngine(), this.scriptEngine);
    }

    /**
     * Adds parameter with specified name and value to the context.
     * This way some predefined variables can be put in runtime context
     * before execution starts.
     * @param name
     * @param value
     */
    public void addVariableToContext(String name, Object value) {
        this.context.put(name, new NodeVariable(value));
    }

    /**
     * Add all map values to the context.
     * @param map
     */
    public void addVariablesToContext(Map map) {
        if (map != null) {
            Iterator iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                this.context.put( entry.getKey(), new NodeVariable(entry.getValue()) );
            }
        }
    }

    public Variable execute(List<IElementDef> ops) {
        this.setStatus(STATUS_RUNNING);

        // inform al listeners that execution is just about to start
        for (ScraperRuntimeListener listener: scraperRuntimeListeners) {
            listener.onExecutionStart(this);
        }

        try {
            for (IElementDef elementDef: ops) {
                BaseProcessor processor = ProcessorResolver.createProcessor(elementDef, this.configuration, this);
                if (processor != null) {
                    processor.run(this, context);
                }
            }
        } finally {
            releaseDBConnections();
        }

        return new EmptyVariable();
    }

    public void execute() {
    	long startTime = System.currentTimeMillis();

        execute( configuration.getOperations() );

        if ( this.status == STATUS_RUNNING ) {
            this.setStatus(STATUS_FINISHED);
        }

        // inform al listeners that execution is finished
        Iterator listenersIterator = this.scraperRuntimeListeners.iterator();
        while (listenersIterator.hasNext()) {
            ScraperRuntimeListener listener = (ScraperRuntimeListener) listenersIterator.next();
            listener.onExecutionEnd(this);
        }

        if ( logger.isInfoEnabled() ) {
            if (this.status == STATUS_FINISHED) {
                logger.info("Configuration executed in " + (System.currentTimeMillis() - startTime) + "ms.");
            } else if (this.status == STATUS_STOPPED) {
                logger.info("Configuration stopped!");
            }
        }
    }
    
    public ScraperContext getContext() {
		return context;
	}

	public ScraperConfiguration getConfiguration() {
        return configuration;
    }

    public String getWorkingDir() {
        return this.workingDir;
    }

    public HttpClientManager getHttpClientManager() {
        return httpClientManager;
    }

    public void addRunningFunction(CallProcessor callProcessor) {
        runningFunctions.push(callProcessor);
    }

    public CallProcessor getRunningFunction() {
        return runningFunctions.isEmpty() ? null : (CallProcessor) runningFunctions.peek();
    }

    public void clearFunctionParams() {
        this.functionParams.clear();
    }

    public void addFunctionParam(String name, Variable value) {
        this.functionParams.put(name, value);
    }

    public Map getFunctionParams() {
        return functionParams;
    }

    public void removeRunningFunction() {
        if (runningFunctions.size() > 0) {
            runningFunctions.pop();
        }
    }
    
    public HttpProcessor getRunningHttpProcessor() {
    	return (HttpProcessor) runningHttpProcessors.peek();
    }
    
    public void setRunningHttpProcessor(HttpProcessor httpProcessor) {
    	runningHttpProcessors.push(httpProcessor);
    }

    public void removeRunningHttpProcessor() {
        if (runningHttpProcessors.size() > 0) {
            runningHttpProcessors.pop();
        }
    }

    public int getRunningLevel() {
        return runningProcessors.size() + 1;
    }

    public boolean isDebugMode() {
        return isDebugMode;
    }

    public void setDebug(boolean debug) {
        this.isDebugMode = debug;
    }

    public ScriptEngine getScriptEngine() {
        return runningFunctions.size() > 0 ? getRunningFunction().getScriptEngine() : this.scriptEngine;
    }

    public synchronized ScriptEngine getScriptEngine(String engineType) {
        ScriptEngine engine = (ScriptEngine) this.usedScriptEngines.get(engineType);
        if (engine == null) {
            engine = configuration.createScriptEngine(this.context, engineType);
            this.usedScriptEngines.put(engineType, engine);
        }

        return engine;
    }

    public Log getLogger() {
        return logger;
    }

    public BaseProcessor getRunningProcessor() {
        return (BaseProcessor) runningProcessors.peek();
    }

    /**
     * @param processor Processor whose parent is needed.
     * @return Parent running processor of the specified running processor, or null if processor is
     * not currently running or if it is top running processor.
     */
    public BaseProcessor getParentRunningProcessor(BaseProcessor processor) {
        List runningProcessorList = runningProcessors.getList();
        int index = CommonUtil.findValueInCollection(runningProcessorList, processor);
        return index > 0 ? (BaseProcessor) runningProcessorList.get(index - 1) : null;
    }

    /**
     * @param processorClazz Class of enclosing running processor.
     * @return Parent running processor in the tree of specified class, or null if it doesn't exist.
     */
    public BaseProcessor getRunningProcessorOfType(Class processorClazz) {
        List runningProcessorList = runningProcessors.getList();
        ListIterator listIterator = runningProcessorList.listIterator(runningProcessors.size());
        while (listIterator.hasPrevious()) {
            BaseProcessor curr = (BaseProcessor) listIterator.previous();
            if (processorClazz.equals(curr.getClass())) {
                return curr;
            }
        }
        return null;
    }

    public RuntimeConfig getRuntimeConfig() {
        return runtimeConfig;
    }

    /**
     * Get connection from the connection pool, and first create one if necessery 
     * @param jdbc Name of JDBC class
     * @param connection JDBC connection string
     * @param username Username
     * @param password Password
     * @return JDBC connection used to access database
     */
    public Connection getConnection(String jdbc, String connection, String username, String password) {
        try {
            String poolKey = jdbc + "-" + connection + "-" + username + "-" + password;
            Connection conn = (Connection) dbPool.get(poolKey);
            if (conn == null) {
                ClassLoaderUtil.registerJDBCDriver(jdbc);
                conn = DriverManager.getConnection(connection, username, password);
                dbPool.put(poolKey, conn);
            }
            return conn;
        }
        catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public void setExecutingProcessor(BaseProcessor processor) {
        this.runningProcessors.push(processor);
        Iterator iterator = this.scraperRuntimeListeners.iterator();
        while (iterator.hasNext()) {
            ScraperRuntimeListener listener = (ScraperRuntimeListener) iterator.next();
            listener.onNewProcessorExecution(this, processor);
        }
    }

    public void finishExecutingProcessor() {
        if (this.runningProcessors.size() > 0) {
            this.runningProcessors.pop();
        }
    }

    public void processorFinishedExecution(BaseProcessor processor, Map properties) {
        Iterator iterator = this.scraperRuntimeListeners.iterator();
        while (iterator.hasNext()) {
            ScraperRuntimeListener listener = (ScraperRuntimeListener) iterator.next();
            listener.onProcessorExecutionFinished(this, processor, properties);
        }
    }

    public void addRuntimeListener(ScraperRuntimeListener listener) {
        this.scraperRuntimeListeners.add(listener);
    }

    public void removeRuntimeListener(ScraperRuntimeListener listener) {
        this.scraperRuntimeListeners.remove(listener);
    }

    public synchronized int getStatus() {
        return status;
    }

    private synchronized void setStatus(int status) {
        this.status = status;
    }

    public void stopExecution() {
        setStatus(STATUS_STOPPED);
    }

    public void exitExecution(String message) {
        setStatus(STATUS_EXIT);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void pauseExecution() {
        if (this.status == STATUS_RUNNING) {
            setStatus(STATUS_PAUSED);

            // inform al listeners that execution is paused
            Iterator listenersIterator = this.scraperRuntimeListeners.iterator();
            while (listenersIterator.hasNext()) {
                ScraperRuntimeListener listener = (ScraperRuntimeListener) listenersIterator.next();
                listener.onExecutionPaused(this);
            }
        }
    }

    public void continueExecution() {
        if (this.status == STATUS_PAUSED) {
            setStatus(STATUS_RUNNING);

            // inform al listeners that execution is continued
            Iterator listenersIterator = this.scraperRuntimeListeners.iterator();
            while (listenersIterator.hasNext()) {
                ScraperRuntimeListener listener = (ScraperRuntimeListener) listenersIterator.next();
                listener.onExecutionContinued(this);
            }
        }
    }

    /**
     * Inform all scraper listeners that an error has occured during scraper execution.
     */
    public void informListenersAboutError(Exception e) {
        setStatus(STATUS_ERROR);

        // inform al listeners that execution is continued
        Iterator listenersIterator = this.scraperRuntimeListeners.iterator();
        while (listenersIterator.hasNext()) {
            ScraperRuntimeListener listener = (ScraperRuntimeListener) listenersIterator.next();
            listener.onExecutionError(this, e);
        }
    }

    /**
     * Releases all DB connections from the pool.
     */
    public void releaseDBConnections() {
        Iterator iterator = dbPool.values().iterator();
        while (iterator.hasNext()) {
            Connection conn = (Connection) iterator.next();
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    throw new DatabaseException(e);
                }
            }
        }
    }

    public void dispose() {
        // empty scraper's variable context
        this.context.clear();

        // free connection with context
        this.context.dispose();

        // releases script engines
        if (this.usedScriptEngines != null) {
            Iterator iterator = this.usedScriptEngines.values().iterator();
            while (iterator.hasNext()) {
                ScriptEngine engine = (ScriptEngine) iterator.next();
                if (engine != null) {
                    engine.dispose();
                }
            }
        }

        Iterator iterator = usedScriptEngines.values().iterator();
        while (iterator.hasNext()) {
            ScriptEngine engine = (ScriptEngine) iterator.next();
            engine.dispose();
        }
    }

}