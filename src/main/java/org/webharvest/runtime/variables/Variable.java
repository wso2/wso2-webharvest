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

import java.util.*;

/**
 * Variables Interface.
 */
public abstract class Variable {

    abstract public byte[] toBinary();

    abstract public byte[] toBinary(String charset);

    abstract public String toString();

    abstract public String toString(String charset);

    abstract public List toList();

    abstract public boolean isEmpty();

    abstract public Object getWrappedObject();

    /**
     * Safely converts this variable to boolean value.
     * @return boolean value
     */
    public boolean toBoolean() {
        Object wrappedObject = getWrappedObject();
        if (wrappedObject == null) {
            return false;
        } else if (wrappedObject instanceof Boolean) {
            return ((Boolean) wrappedObject).booleanValue(); 
        } else {
            String strValue = toString().trim();
            return "true".equalsIgnoreCase(strValue) || "yes".equalsIgnoreCase(strValue) || "1".equalsIgnoreCase(strValue);
        }
    }

    /**
     * Safely converts this variable to integer value.
     * @return int value
     */
    public int toInt() {
        Object wrappedObject = getWrappedObject();
        if (wrappedObject == null) {
            return 0;
        } else if (wrappedObject instanceof Number) {
            return ((Number)wrappedObject).intValue();
        } else if (wrappedObject instanceof Boolean) {
            return ((Boolean)wrappedObject).booleanValue() ? 1 : 0;
        } else {
            return Integer.parseInt( toString().trim() );
        }
    }

    /**
     * Safely converts this variable to long value.
     * @return long value
     */
    public long toLong() {
        Object wrappedObject = getWrappedObject();
        if (wrappedObject == null) {
            return 0L;
        } else if (wrappedObject instanceof Number) {
            return ((Number)wrappedObject).longValue();
        } else if (wrappedObject instanceof Boolean) {
            return ((Boolean)wrappedObject).booleanValue() ? 1L : 0L;
        } else {
            return Long.parseLong( toString().trim() );
        }
    }

    /**
     * Safely converts this variable to double value.
     * @return double value
     */
    public double toDouble() {
        Object wrappedObject = getWrappedObject();
        if (wrappedObject == null) {
            return 0d;
        } else if (wrappedObject instanceof Number) {
            return ((Number)wrappedObject).doubleValue();
        } else if (wrappedObject instanceof Boolean) {
            return ((Boolean)wrappedObject).booleanValue() ? 1d : 0d;
        } else {
            return Double.parseDouble( toString().trim() );
        }
    }

    /**
     * Safely converts this variable to array of objects.
     * @return array of objects
     */
    public Object[] toArray() {
        Object wrappedObject = getWrappedObject();
        if (wrappedObject == null) {
            return new Object[] {};
        } else if (wrappedObject instanceof Object[]) {
            return (Object[]) wrappedObject;
        } else if (wrappedObject instanceof Collection) {
            Collection collection = (Collection) wrappedObject;
            Object result[] = new Object[collection.size()];
            int index = 0;
            Iterator iterator = collection.iterator();
            while (iterator.hasNext()) {
                result[index] = iterator.next();
                index++;
            }

            return result;
        } else {
            return new Object[] {wrappedObject};
        }
    }

}