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
package org.webharvest.runtime.scripting;

import bsh.EvalError;
import bsh.Interpreter;
import org.webharvest.exception.ScriptException;

import java.util.Map;

/**
 * BeanShell scripting engine.
 */
public class BeanShellScriptEngine extends ScriptEngine {

    private Interpreter beanShellInterpreter = new Interpreter();

    /**
     * Constructor - initializes context used in engine.
     * @param context
     */
    public BeanShellScriptEngine(Map context) {
        super(context);
        this.beanShellInterpreter.getNameSpace().importCommands("org.webharvest.runtime.scripting");
        try {
            this.beanShellInterpreter.set(CONTEXT_VARIABLE_NAME, this.context);
        } catch (EvalError e) {
            throw new ScriptException("Cannot set Web-Harvest context in scripter: " + e.getMessage(), e);
        }
    }

    /**
     * Sets variable in scripter context.
     * @param name
     * @param value
     */
    public void setVariable(String name, Object value) {
        try {
            this.beanShellInterpreter.set(name, value);
        } catch (EvalError e) {
            throw new ScriptException("Cannot set variable in scripter: " + e.getMessage(), e);
        }
    }

    /**
     * Evaluates specified expression or code block.
     * @return value of evaluation or null if there is nothing.
     */
    public Object eval(String expression) {
        pushAllVariablesFromContextToScriptEngine();

        try {
            return this.beanShellInterpreter.eval(expression);
        } catch (EvalError e) {
            throw new ScriptException("Error during script execution: " + e.getMessage(), e);
        }
    }

    public void dispose() {
        this.beanShellInterpreter.getNameSpace().clear();
        super.dispose();
    }

}