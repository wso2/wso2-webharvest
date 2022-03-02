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
package org.webharvest.definition;

import org.webharvest.utils.CommonUtil;

/**
 * Definition of HTTP processor.
 */
public class HttpDef extends BaseElementDef {

    public static final String METHOD_GET = "get";
    public static final String METHOD_POST = "post";

    private String method;
    private String multipart;
    private String url;
    private String charset;
    private String username;
    private String password;
    private String cookiePolicy;

    public HttpDef(XmlNode xmlNode) {
        super(xmlNode);

        this.method = CommonUtil.nvl( xmlNode.getAttribute("method"), METHOD_GET );
        this.multipart = CommonUtil.nvl( xmlNode.getAttribute("multipart"), "false" );
        this.url = xmlNode.getAttribute("url");
        this.charset = xmlNode.getAttribute("charset");
        this.username = xmlNode.getAttribute("username");
        this.password = xmlNode.getAttribute("password");
        this.cookiePolicy = xmlNode.getAttribute("cookie-policy");
    }

    public String getMethod() {
        return method;
    }

    public String getMultipart() {
        return multipart;
    }

    public String getUrl() {
        return url;
    }

    public String getCharset() {
        return charset;
    }

	public String getPassword() {
		return password;
	}

	public String getUsername() {
		return username;
	}

    public String getCookiePolicy() {
        return cookiePolicy;
    }

    public String getShortElementName() {
        return "http";
    }
    
}