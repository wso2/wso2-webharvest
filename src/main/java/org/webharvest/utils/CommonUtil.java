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
package org.webharvest.utils;

import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.query.QueryResult;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Type;
import org.webharvest.exception.VariableException;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.runtime.variables.NodeVariable;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.URL;
import java.util.*;

/**
 * Basic evaluation utilities
 */
public class CommonUtil {

    /**
     * Contains pair of intger values
     */
    public static class IntPair {
        public int x;
        public int y;

        public IntPair() {
        }

        public IntPair(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public String toString() {
            return "x = " + x + ", y = " + y;
        }

        public void defineFromString(String s, char separator, int maxValue) {
            int columnIndex = s.indexOf(separator);
            if (columnIndex == -1) {
                x = Integer.parseInt(s);
                y = x;
            } else {
                if (columnIndex == 0) {
                    x = 1;
                    String s2 = s.substring(1);
                    y = "".equals(s2) ? maxValue : Integer.parseInt(s2);
                } else if (columnIndex == s.length()-1) {
                    x = Integer.parseInt( s.substring(0, s.length()-1) );
                    y = maxValue;
                } else {
                    String s1 = s.substring(0, columnIndex);
                    String s2 = s.substring(columnIndex + 1);
                    x = "".equals(s1) ? 1 : Integer.parseInt(s1);
                    y = "".equals(s2) ? maxValue : Integer.parseInt(s2);
                }
            }
        }
    }

    public static boolean isEmptyString(Object o) {
        return o == null || "".equals(o.toString().trim());
    }

    public static String nvl(Object value, String defaultValue) {
        return value != null ? value.toString() : defaultValue;
    }

    public static String adaptFilename(String filePath) {
        return filePath == null ? null : filePath.replace('\\', '/');
    }

    public static boolean isEmpty(String s) {
        return s == null || "".equals(s);
    }

    /**
     * Checks if given string is valid XML identifier, i.e. it can be valid XML tag
     * or attribute name.
     * @param name String to be checked
     * @return True if string is valid XML identifier, false otherwise.
     */
    public static boolean isValidXmlIdentifier(String name) {
        if ( !isEmpty(name) ) {
            if ( Character.isJavaIdentifierStart(name.charAt(0)) ) {
                for (int i = 1; i < name.length(); i++ )  {
                    char ch = name.charAt(i);
                    if ( !Character.isJavaIdentifierPart(ch) && ch != '-' ) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if specified file path is absolute. Criteria for recogning absolute file paths is
     * that i starts with /, \, or X: where X is some letter.
     * @param path
     * @return True, if specified filepath is absolute, false otherwise.
     */
    public static boolean isPathAbsolute(String path) {
        if (path == null) {
            return false;
        }

        path = adaptFilename(path);
        int len = path.length();

        return ( len >= 1 && path.startsWith("/") ) ||
               ( len >= 2 && Character.isLetter(path.charAt(0)) && path.charAt(1) == ':' ); 
    }

    /**
     * For the goven working path and file path returns absolute file path.
     * @param workingPath
     * @param filePath
     * @return Absolute path of the second parameter according to absolute working path.
     */
    public static String getAbsoluteFilename(String workingPath, String filePath) {
    	filePath = adaptFilename(filePath);

        // if file path is absolute, then return only filePath parameter
        if (isPathAbsolute(filePath)) {
            return filePath;
        } else {
            workingPath = adaptFilename(workingPath);
            if (workingPath.endsWith("/")) {
                workingPath = workingPath.substring( 0, workingPath.length() - 1 );
            }
            return workingPath + "/" + filePath;
        }
    }
    
	/**
	 * Extracts a filename and directory from an absolute path.
	 */
	public static String getDirectoryFromPath(String path) {
		path = adaptFilename(path);
		int index = path.lastIndexOf("/");

		return path.substring(0, index);
	}

	/**
	 * Extracts a filename from an absolute path.
	 */
	public static String getFileFromPath(String path) {
		int i1 = path.lastIndexOf("/");
		int i2 = path.lastIndexOf("\\");
		if (i1 > i2) {
			return path.substring(i1+1);
		}
		return path.substring(i2+1);
	}

    /**
     * Returns class name without packages for the specified object
     */
    public static String getClassName(Object o) {
        if (o != null) {
            String processorClassName = o.getClass().getName();
            int dotIndex = processorClassName.lastIndexOf('.');
            if (dotIndex >= 0) {
                processorClassName = processorClassName.substring(dotIndex+1);
            }

            return processorClassName;
        } else {
            return "null value";
        }
    }

    public static String replicate(String s, int count) {
        String result = "";
        for (int i = 1; i <= count; i++) {
            result += s;
        }

        return result;
    }
    
    private static String encodeUrlParam(String value, String charset) throws UnsupportedEncodingException {
        if (value == null) {
            return "";
        }

        try {
    		String decoded = URLDecoder.decode(value, charset);
    		
    		String result = "";
    		for (int i = 0; i < decoded.length(); i++) {
    			char ch = decoded.charAt(i);
    			result += (ch == '#') ? "#" : URLEncoder.encode(String.valueOf(ch), charset);
    		}

    		return result;
    	} catch (IllegalArgumentException e) {
    		return value;
    	}
    }

    public static String encodeUrl(String url, String charset) {
        if (url == null) {
            return "";
        }
        
        int index = url.indexOf("?");
        if (index >= 0) {
            try {
                String result = url.substring(0, index+1);
                String paramsPart = url.substring(index+1);
                StringTokenizer tokenizer = new StringTokenizer(paramsPart, "&");
                while (tokenizer.hasMoreTokens()) {
                    String definition = tokenizer.nextToken();
                    int eqIndex = definition.indexOf("=");
                    if (eqIndex >= 0) {
                        String paramName = definition.substring(0, eqIndex);
                        String paramValue = definition.substring(eqIndex + 1);
                        result += paramName + "=" + encodeUrlParam(paramValue, charset) + "&";
                    } else {
                        result += encodeUrlParam(definition, charset) + "&";
                    }
                }

                if( result.endsWith("&") ) {
                    result = result.substring(0, result.length() - 1);
                }

                return result;
            } catch (UnsupportedEncodingException e) {
                throw new VariableException("Charset " + charset + " is not supported!", e);
            }
        }

        return url;
    }
    
    /**
     * Checks if specified string value represents boolean true value.
     * @return If specified string equals (ignoring case) to 1, true or yes then
     *         true, otherwise false. 
     */
    public static boolean isBooleanTrue(String value) {
    	if (value != null) {
    		return "1".equals(value) || "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value);
    	}
    	
    	return false;
    }

    /**
     * Reads boolean value from string
     * @param value
     * @param defaultValue value to be returned if string value is not recognized      
     */
    public static boolean getBooleanValue(String value, boolean defaultValue) {
        if ( "1".equals(value) || "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) ) {
            return true;
        } else if ( "0".equals(value) || "false".equalsIgnoreCase(value) || "no".equalsIgnoreCase(value) ) {
            return false;
        }

        return defaultValue;
    }

    /**
     * Reads integer value from string
     * @param value
     * @param defaultValue value to be returned if string value is not valid integer
     */
    public static int getIntValue(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Reads double value from string
     * @param value
     * @param defaultValue value to be returned if string value is not valid double
     */
    public static double getDoubleValue(String value, double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Escapes XML string - special characters: &'"<> are
     * replaced with XML escape sequences: &amp; &apos; &quot; &lt; &gt;
     */
    public static String escapeXml(String s) {
        if (s != null) {
            StringBuffer result = new StringBuffer(s);
            int index = 0;
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                if (ch == '&') {
                    String sub = s.substring(i);
                    if ( !sub.startsWith("&amp;") &&
                         !sub.startsWith("&apos;") &&
                         !sub.startsWith("&gt;") &&
                         !sub.startsWith("&lt;") &&
                         !sub.startsWith("&quot;") ) {
                        result.replace(index, index+1, "&amp;");
                        index += 5;
                    } else {
                        index++;
                    }
                } else if (ch == '\'') {
                    result.replace(index, index+1, "&apos;");
                    index += 6;
                } else if (ch == '>') {
                    result.replace(index, index+1, "&gt;");
                    index += 4;
                } else if (ch == '<') {
                    result.replace(index, index+1, "&lt;");
                    index += 4;
                } else if (ch == '\"') {
                    result.replace(index, index+1, "&quot;");
                    index += 6;
                } else {
                    index++;
                }
            }

            return result.toString();
        }

        return null;
    }

    /**
     * Serializes item after XPath or XQuery processor execution using Saxon.
     */
    public static String serializeItem(Item item) throws XPathException {
    	if (item instanceof NodeInfo) {
    		int type = ((NodeInfo)item).getNodeKind();
            if (type == Type.DOCUMENT || type == Type.ELEMENT) {
	            Properties props = new Properties();
	            props.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	            props.setProperty(OutputKeys.INDENT, "yes");

                StringWriter stringWriter = new java.io.StringWriter();
                QueryResult.serialize((NodeInfo)item, new StreamResult(stringWriter), props);
                stringWriter.flush();
                return stringWriter.toString().replaceAll(" xmlns=\"http\\://www.w3.org/1999/xhtml\"", "");
            }
    	}
    	
    	return item.getStringValue();
    }
    
    public static String readStringFromFile(File file, String encoding) throws IOException {
    	if (!file.exists()) {
    		throw new IOException("File doesn't exist!");
		}
    	
		long fileLen = file.length();
		if (fileLen <= 0L) {
			if (file.exists()) {
				return ""; // empty file
			}
			return null; // all other file len problems
		}
		if (fileLen > Integer.MAX_VALUE) { // max String size
			throw new IOException("File too big for loading into a String!");
		}

		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader brin = null;

		int length = (int) fileLen;
		char[] buf = null;
		int realSize = 0;
		try {
			fis = new FileInputStream(file);
			isr = new InputStreamReader(fis, encoding);
			brin = new BufferedReader(isr, 64 * 1024);
			buf = new char[length];
			int c;
			while ((c = brin.read()) != -1) {
				buf[realSize] = (char) c;
				realSize++;
			}
		} finally {
			if (brin != null) {
				brin.close();
				isr = null;
				fis = null;
			}
			if (isr != null) {
				isr.close();
				fis = null;
			}
			if (fis != null) {
				fis.close();
			}
		}
		return new String(buf, 0, realSize);
	}

    /**
     * Saves specified content to the file with specified charset.
     * @param file
     * @param content
     * @param charset
     * 
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public static void saveStringToFile(File file, String content, String charset) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        byte[] data = content.getBytes(charset);

        out.write(data);

        out.flush();
        out.close();
    }

    public static byte[] readBytesFromFile(File file) throws IOException {
		FileInputStream fileinputstream = new FileInputStream(file);
		long l = file.length();

        if (l > Integer.MAX_VALUE) {
			throw new IOException("File too big for loading into a byte array!");
		}
        
        byte byteArray[] = new byte[(int) l];

		int i = 0;

		for (int j = 0; (i < byteArray.length) && (j = fileinputstream.read(byteArray, i, byteArray.length - i)) >= 0; i += j);

		if (i < byteArray.length) {
			throw new IOException("Could not completely read the file " + file.getName());
		}
		fileinputstream.close();
		return byteArray;
	}

    /**
     * Checks if specified link is full URL.
     * @param link
     * @return True, if full URl, false otherwise.
     */
    public static boolean isFullUrl(String link) {
        if (link == null) {
            return false;
        }
        link = link.trim().toLowerCase();
        return link.startsWith("http://") || link.startsWith("https://") || link.startsWith("file://");
    }
    
    /**
     * Calculates full URL for specified page URL and link
     * which could be full, absolute or relative like there can 
     * be found in A or IMG tags. 
     */
    public static String fullUrl(String pageUrl, String link) {
    	if ( isFullUrl(link) ) {
    		return link;
    	} else if ( link != null && link.startsWith("?") ) {
            int qindex = pageUrl.indexOf('?');
            int len = pageUrl.length();
            if (qindex < 0) {
                return pageUrl + link;
            } else if (qindex == len - 1) {
                return pageUrl.substring(0, len - 1) + link;
            } else {
                return pageUrl + "&" + link.substring(1);
            }
        }
    	
    	boolean isLinkAbsolute = link.startsWith("/");
    	
    	if ( !isFullUrl(pageUrl) ) {
    		pageUrl = "http://" + pageUrl;
    	}
    	
    	int slashIndex = isLinkAbsolute ? pageUrl.indexOf("/", 8) : pageUrl.lastIndexOf("/");
    	if (slashIndex <= 8) {
    		pageUrl += "/";
    	} else {
    		pageUrl = pageUrl.substring(0, slashIndex+1);
    	}
    	
    	return isLinkAbsolute ? pageUrl + link.substring(1) : pageUrl + link; 
    }

    /**
     * Creates appropriate AbstractVariable instance for the specified object.
     * For colleactions and arrays ListVariable instance is returned,
     * for null it is an EmptyVariable, and for others it is NodeVariable
     * that wraps specified object. 
     * @param value
     */
    public static Variable createVariable(Object value) {
        if (value instanceof Variable) {
            return (Variable) value;
        } else if (value == null) {
            return new EmptyVariable();
        } else if (value instanceof Collection) {
            Collection collection = (Collection) value;
            return new ListVariable(new ArrayList(collection));
        } else if (value instanceof Object[]) {
            List list = Arrays.asList( (Object[]) value );
            return new ListVariable(list);
        } else {
            return new NodeVariable(value);
        }
    }

    /**
     * Reads content from specified URL
     * @param url
     * @throws IOException
     * @return Read content as string.
     */
    public static String readStringFromUrl(URL url) throws IOException {
        StringBuffer buffer = new StringBuffer();
    	BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
    	int ch;
        while ( (ch = in.read()) != -1 ) {
            buffer.append( (char)ch );
        }
        in.close();

        return buffer.toString();
    }

    /**
     * Counts number of specified characters in give text.
     * @param text Text to be parsed
     * @param ch Character to be counted
     * @param from Text offset
     * @param to Text end
     * @return Number of character occurences in given text.
     */
    public static int countChars(String text, char ch, int from, int to) {
        int textLen = text.length();
        if (from < 0) {
            from = 0;
        }
        if (to >= textLen) {
            to = textLen - 1;
        }

        int count = 0;
        for (int i = from; i <= to; i++) {
            if (text.charAt(i) == ch) {
                count++;
            }
        }

        return count;
    }

    /**
     * Checks if specified string exists in given array
     * @param array  Array of strings
     * @param s String to be looked for in array
     * @param caseSensitive Tells whether search is case sensitive
     * @return True if string is found in array, false otherwise
     */
    public static boolean existsInStringArray(String[] array, String s, boolean caseSensitive) {
        if (s != null && array != null) {
            for (int i = 0; i < array.length; i++) {
                if ( (caseSensitive && s.equals(array[i])) || s.equalsIgnoreCase(array[i]) ) {
                    return true;
                }

            }
        }
        return false;
    }

    /**
     * Tokenize given string for specified delimiter(s).
     * @param s String to be tokenized
     * @param delimiters Delimiter character(s)
     * @return Array of token strings      
     */
    public static String[] tokenize(String s, String delimiters) {
        if (s == null) {
            return new String[] {};
        }

        StringTokenizer tokenizer = new StringTokenizer(s, delimiters);
        String result[] = new String[tokenizer.countTokens()];
        int index = 0;
        while (tokenizer.hasMoreTokens()) {
            result[index++] = tokenizer.nextToken();
        }

        return result;
    }

    public static String[] tokenize(String s, String delimiters, boolean trimTokens, boolean allowEmptyTokens) {
        if (s == null) {
            return new String[] {};
        }

        StringTokenizer tokenizer = new StringTokenizer(s, delimiters, true);
        List<String> tokenList = new LinkedList<String>();
        boolean lastWasSeparator = false;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            boolean isSeparator = delimiters.indexOf(token) >= 0;
            if (trimTokens) {
                token = token.trim();
            }
            if (isSeparator) {
                if (lastWasSeparator && allowEmptyTokens) {
                    tokenList.add("");
                }
            } else if (!"".equals(token) || allowEmptyTokens) {
                tokenList.add(token);
            }
            lastWasSeparator = isSeparator;
        }
        if (lastWasSeparator && allowEmptyTokens) {
            tokenList.add("");
        }

        return tokenList.toArray(new String[tokenList.size()]);
    }

    /**
     * For the given string creates valid identifier name. All invalid characters
     * are transformed to underscores, and valid characters are preserved.
     * @param value String to be transformed to valid identifier
     * @return Valid identifier name made of specified string.
     */
    public static String getValidIdentifier(String value) {
        if (value == null) {
            return "_";
        }
        StringBuffer validIdentifier = new StringBuffer();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if ( (i == 0 && !Character.isJavaIdentifierStart(ch)) || !Character.isJavaIdentifierPart(ch) ) {
                ch = '_';
            }
            validIdentifier.append(ch);
        }
        return validIdentifier.length() == 0 ? "_" : validIdentifier.toString();
    }

    /**
     * Searches specified value in given collection
     * @param c Collection to be searched
     * @param value Object searched for
     * @return First index in collection of object found, or -1 if collection doesn't contain it
     */
    public static int findValueInCollection(Collection c, Object value) {
        Iterator iterator = c.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            Object curr = iterator.next();
            if ( value == curr || (value != null && curr != null && value.equals(curr)) ) {
                return index;
            }
            index++;
        }
        return -1;
    }

}