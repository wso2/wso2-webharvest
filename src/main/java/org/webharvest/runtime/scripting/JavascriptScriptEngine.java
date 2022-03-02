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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.Map;

/**
 * javascript scripting engine based on Rhino.
 */
public class JavascriptScriptEngine extends ScriptEngine {

    private Context javascriptContext = null;
    private Scriptable scope = null;

    /**
     * Constructor - initializes context used in engine.
     * @param context
     */
    public JavascriptScriptEngine(Map context) {
        super(context);
    }

    private synchronized void initContextIfNeeded() {
        if (this.javascriptContext == null) {
            this.javascriptContext = Context.enter();
            this.scope = javascriptContext.initStandardObjects();
        }
    }

    /**
     * Sets variable in scripter context.
     * @param name
     * @param value
     */
    public void setVariable(String name, Object value) {
        initContextIfNeeded();
        Object wrappedOut = Context.javaToJS(value, scope);
        ScriptableObject.putProperty(this.scope, name, wrappedOut);
    }

    /**
     * Evaluates specified expression or code block.
     * @return value of evaluation or null if there is nothing.
     */
    public Object eval(String expression) {
        initContextIfNeeded();
        pushAllVariablesFromContextToScriptEngine();
        return this.javascriptContext.evaluateString(scope, expression, "<cmd>", 1, null);
    }

}