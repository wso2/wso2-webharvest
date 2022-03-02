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
package org.webharvest.runtime.web;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethodBase;
import org.webharvest.utils.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Class defines http server response. 
 */
public class HttpResponseWrapper {
	
	private String charset;
	private String mimeType;
	private byte[] body;
	private KeyValuePair<String> headers[];
    private int statusCode;
    private String statusText;

    /**
	 * Constructor - defines response result based on specified HttpMethodBase instance.
	 * @param method
	 */
	public HttpResponseWrapper(HttpMethodBase method) {
        try {
			this.body = method.getResponseBody();
        } catch (IOException e) {
			// todo: handle exception
            e.printStackTrace();
        }
		
		Header[] headerArray = method.getResponseHeaders();
        if (headerArray != null) {
            headers = new KeyValuePair[headerArray.length];
        	for (int i = 0; i < headerArray.length; i++) {
        		String currName = headerArray[i].getName();
        		String currValue = headerArray[i].getValue();
        		headers[i] = new KeyValuePair<String>(currName, currValue);
        		if ("content-type".equalsIgnoreCase(currName)) {
        			int index = currValue.indexOf(';');
        			this.mimeType = index > 0 ? currValue.substring(0, index) : currValue;
        		}
        	}
        }

        this.charset = method.getResponseCharSet();
        this.statusCode = method.getStatusCode();
        this.statusText = method.getStatusText();

    }

    public long getContentLength() {
        return this.body == null ? 0 : this.body.length;
    }

    public String getCharset() {
		return this.charset;
	}
	
	public String getMimeType() {
		return this.mimeType;
	}
	
	public byte[] getBody() {
		return this.body;
	}
	
	public InputStream getBodyAsInputStream() {
		return new ByteArrayInputStream(body);
	}
	
	public KeyValuePair<String>[] getHeaders() {
		return this.headers;
	}

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

}