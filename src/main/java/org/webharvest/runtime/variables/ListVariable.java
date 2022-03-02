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
package org.webharvest.runtime.variables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * List variable - String wrapper.
 */
public class ListVariable extends Variable {

    private List<Variable> list;

    private String cachedStringRepresentation = null;

    public ListVariable() {
        this.list = new ArrayList<Variable>();
    }

    public ListVariable(List list) {
        this.list = new ArrayList<Variable>();
        
        if (list != null) {
    		Iterator it = list.iterator();
    		while (it.hasNext()) {
                Object object = it.next();
                Variable var = object instanceof Variable ? (Variable) object : new NodeVariable(object);
    			if ( !var.isEmpty() ) {
    				this.list.add(var);
    			}
    		}
    	}
    }

    public String toString() {
        if (cachedStringRepresentation == null) {
            StringBuffer buffer = new StringBuffer();

            Iterator it = list.iterator();
            while (it.hasNext()) {
                Variable var = (Variable) it.next();
                String value = var.toString();
                if (value.length() != 0) {
                    if (buffer.length() != 0) {
                        buffer.append('\n');
                    }
                    buffer.append(value);
                }
            }

            cachedStringRepresentation = buffer.toString();
        }

        return cachedStringRepresentation;
    }

    public String toString(String charset, String delimiter) {
        StringBuffer buffer = new StringBuffer();

        for (Variable var: list) {
            String value = var.toString(charset);
            if (value.length() != 0) {
                if (buffer.length() != 0) {
                    buffer.append(delimiter);
                }
                buffer.append(value);
            }
        }

        return buffer.toString();
    }

    public String toString(String charset) {
        return toString(charset, "\n");
    }

    public byte[] toBinary(String charset) {
        byte[] result = null;
        
        Iterator it = list.iterator();
        while (it.hasNext()) {
        	Variable currVar = (Variable) it.next();
        	byte[] curr = charset == null ? currVar.toBinary() : currVar.toBinary(charset);
        	if (curr != null) {
        		if (result == null) {
        			result = curr;
        		} else {
        			byte[] newResult = new byte[result.length + curr.length];
        			System.arraycopy(result, 0, newResult, 0, result.length);
        			System.arraycopy(curr, 0, newResult, result.length, curr.length);
        			result = newResult;
        		}
        	}
        }

        return result;
    }

    public byte[] toBinary() {
        return toBinary(null);
    }

    public List toList() {
        return list;
    }

    public boolean isEmpty() {
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Variable var =  (Variable) it.next();
            if (!var.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public void addVariable(Variable variable) {
        // in order string value needs to be recached
        cachedStringRepresentation = null;

        if (variable instanceof ListVariable) {
            list.addAll( ((ListVariable)variable).getList() );
        } else {
            list.add(variable == null ? EmptyVariable.INSTANCE : variable);
        }
    }

    private Collection getList() {
        return this.list;
    }
    
    /**
     * Checks if list contains specified object's string representation
     * @param item
     */
    public boolean contains(Object item) {
    	Iterator it = list.iterator();
    	while (it.hasNext()) {
    		Variable currVariable = (Variable) it.next();
    		if ( currVariable != null && currVariable.toString().equals(item.toString()) ) {
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    public Object getWrappedObject() {
        return this.list;
    }

    public Variable get(int index) {
        return list.get(index);
    }

}