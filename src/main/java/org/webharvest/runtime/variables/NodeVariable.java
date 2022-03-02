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

import org.webharvest.exception.*;

import java.util.ArrayList;
import java.util.List;
import java.io.*;

/**
 * Node variable - Single node wrapper.
 */
public class NodeVariable extends Variable {

    private Object data;

    public NodeVariable(Object data) {
        this.data = data;
    }

    public String toString() {
        if (data == null) {
            return "";
        } else if (data instanceof byte[]) {
            return new String((byte[]) data);
        } else {
            return data.toString();
        }
    }

    public String toString(String charset) {
        if (data == null) {
            return "";
        } else if (data instanceof byte[]) {
            try {
                return new String((byte[]) data, charset);
            } catch (UnsupportedEncodingException e) {
                throw new VariableException(e);
            }
        } else {
            return data.toString();
        }
    }

    public byte[] toBinary() {
        if (data == null) {
            return new byte[] {};
        } else if (data instanceof byte[]) {
            return (byte[]) data;
        } else {
            return data.toString().getBytes();
        }
    }

    public byte[] toBinary(String charset) {
        if (charset == null || data == null || data instanceof byte[]) {
            return toBinary();
        } else {
            try {
                return data.toString().getBytes(charset);
            } catch (UnsupportedEncodingException e) {
                throw new VariableException(e);
            }
        }
    }

    public List toList() {
        List list = new ArrayList();
        if (!isEmpty()) {
        	list.add(this);
        }

        return list;
    }

    public boolean isEmpty() {
        return (data == null) || ( "".equals(toString()) );
    }

    public Object getWrappedObject() {
        return this.data;
    }

}