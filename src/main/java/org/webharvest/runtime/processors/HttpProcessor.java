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

import org.apache.commons.httpclient.NameValuePair;
import org.webharvest.definition.HttpDef;
import org.webharvest.exception.HttpException;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.scripting.ScriptEngine;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.web.*;
import org.webharvest.utils.*;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;

/**
 * Http processor.
 */
public class HttpProcessor extends BaseProcessor {

    private static final String HTML_META_CHARSET_REGEX =
        "(<meta\\s*http-equiv\\s*=\\s*(\"|')content-type(\"|')\\s*content\\s*=\\s*(\"|')text/html;\\s*charset\\s*=\\s*(.*?)(\"|')/?>)";

    private HttpDef httpDef;
    
    private Map<String, HttpParamInfo> httpParams = new LinkedHashMap<String, HttpParamInfo>();
    private Map httpHeaderMap = new HashMap();

    public HttpProcessor(HttpDef httpDef) {
        super(httpDef);
        this.httpDef = httpDef;
    }

    public Variable execute(Scraper scraper, ScraperContext context) {
    	scraper.setRunningHttpProcessor(this);

        ScriptEngine scriptEngine = scraper.getScriptEngine();
        String url = BaseTemplater.execute( httpDef.getUrl(), scriptEngine);
        String method = BaseTemplater.execute( httpDef.getMethod(), scriptEngine);
        String multipart = BaseTemplater.execute( httpDef.getMultipart(), scriptEngine);
        boolean isMultipart = CommonUtil.getBooleanValue(multipart, false);
        String specifiedCharset = BaseTemplater.execute( httpDef.getCharset(), scriptEngine);
        String username = BaseTemplater.execute( httpDef.getUsername(), scriptEngine);
        String password = BaseTemplater.execute( httpDef.getPassword(), scriptEngine);
        String cookiePolicy = BaseTemplater.execute( httpDef.getCookiePolicy(), scriptEngine);

        String charset = specifiedCharset;

        if (charset == null) {
            charset = scraper.getConfiguration().getCharset();
        }
        
        // executes body of HTTP processor
        new BodyProcessor(httpDef).execute(scraper, context);

        HttpClientManager manager = scraper.getHttpClientManager();
        manager.setCookiePolicy(cookiePolicy);

        HttpResponseWrapper res = manager.execute(method, isMultipart, url, charset, username, password, httpParams, httpHeaderMap);

        scraper.removeRunningHttpProcessor();

        String mimeType = res.getMimeType();

        long contentLength = res.getContentLength();
        if ( scraper.getLogger().isInfoEnabled() ) {
            scraper.getLogger().info("Downloaded: " + url + ", mime type = " + mimeType + ", length = " + contentLength + "B.");
        }

        Variable result;

        String responseCharset = res.getCharset();
        byte[] responseBody = res.getBody();

        if ( mimeType == null || mimeType.indexOf("text") >= 0 || mimeType.indexOf("xml") >= 0 || mimeType.indexOf("javascript") >= 0 ) {
            String text = "";
            try {
                // resolvs charset in the following way:
                //    1. if explicitely defined as charset attribute in http processor, then use it
                //    2. if it is HTML document, reads first KB from response's body as ASCII stream
                //       and tries to find meta tag with specified charset
                //    3. use charset from response's header
                //    4. uses default charset for the configuration
                if (specifiedCharset == null) {
                    if (responseCharset != null && Charset.isSupported(responseCharset)) {
                        charset = responseCharset;
                    }
                    if ( "text/html".equalsIgnoreCase(res.getMimeType()) ) {
                        String firstBodyKb = new String(responseBody, 0, Math.min(responseBody.length, 1024), "ASCII");
                        Matcher matcher = Pattern.compile(HTML_META_CHARSET_REGEX, Pattern.CASE_INSENSITIVE).matcher(firstBodyKb);
                        if (matcher.find()) {
                            String foundCharset = matcher.group(5);
                            try {
                                if (Charset.isSupported(foundCharset)) {
                                    charset = foundCharset;
                                }
                            } catch(IllegalCharsetNameException e) {
                                // do nothing - charset will not be set here
                            }
                        }
                    }
                }
                text = new String(responseBody, charset);
            } catch (UnsupportedEncodingException e) {
                throw new HttpException("Charset " + charset + " is not supported!", e);
            }
            
            result =  new NodeVariable(text);
        } else {
            result = new NodeVariable(responseBody);
        }

        this.setProperty("URL", url);
        this.setProperty("Method", method);
        this.setProperty("Multipart", String.valueOf(isMultipart));
        this.setProperty("Charset", charset);
        this.setProperty("Content length", String.valueOf(contentLength));
        this.setProperty("Status code", res.getStatusCode());
        this.setProperty("Status text", res.getStatusText());

        KeyValuePair<String>[] headerPairs = res.getHeaders();
        if (headerPairs != null) {
            int index = 1;
            for (KeyValuePair<String> pair: headerPairs) {
                this.setProperty("HTTP header [" + index + "]: " + pair.getKey(), pair.getValue());
                index++;
            }
        }

        return result;
    }
    
    protected void addHttpParam(String name, boolean isFile, String fileName, String contentType, Variable value) {
        HttpParamInfo httpParamInfo = new HttpParamInfo(name, isFile, fileName, contentType, value);
    	httpParams.put(name, httpParamInfo);
    }
    
    protected void addHttpHeader(String name, String value) {
    	httpHeaderMap.put(name, value);
    }

}