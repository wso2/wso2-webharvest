/*
 * $Id: XMLInputReader.java,v 1.2 2006/01/14 16:12:01 edankert Exp $
 *
 * Copyright (c) 2002 - 2005, Edwin Dankert
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright 
 *   notice, this list of conditions and the following disclaimer in the 
 *   documentation and/or other materials provided with the distribution. 
 * * Neither the name of 'Edwin Dankert' nor the names of its contributors 
 *   may  be used to endorse or promote products derived from this software 
 *   without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.bounce.text.xml;

import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * A Reader for XML input, which can handle escape characters.
 * 
 * <p>
 * <b>Note: </b> The XML Editor package is based on the JavaEditorKit example as
 * described in the article <i>'Customizing a Text Editor' </i> by <b>Timothy
 * Prinzing </b>. See:
 * http://java.sun.com/products/jfc/tsc/articles/text/editor_kit/
 * </p>
 * 
 * @version $Revision: 1.2 $, $Date: 2006/01/14 16:12:01 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class XMLInputReader extends FilterReader {

    private static final int BUFFERLEN = 10240;

    private final char buffer[] = new char[BUFFERLEN];

    private XMLInputStream stream = null;

    long pos = 0;

    private long chpos = 0x100000000L;
    private int pushBack = -1;
    private int lastChar = -1;
    private int currentIndex = 0;
    private int numChars = 0;

    /**
     * Constructs the new input stream reader out of the Xml input strem.
     * 
     * @param inputstream the XML input stream.
     * 
     * @throws UnsupportedEncodingException
     */
    public XMLInputReader( XMLInputStream inputstream) throws UnsupportedEncodingException {
        super( new InputStreamReader( inputstream));

        stream = inputstream;
    }

    /**
     * Sets the scan range of the reader.
     * 
     * @param start
     *            the start position.
     * @param end
     *            the end position.
     */
    public void setRange( int start, int end) {
        stream.setRange( start, end);

        pos = 0;
        chpos = 0x100000000L;
        pushBack = -1;
        lastChar = -1;
        currentIndex = 0;
        numChars = 0;
    }
    
    /**
     * Reads one character from the stream and increases the index.
     * 
     * @return the character or -1 for an eof.
     * 
     * @throws IOException
     */
    public int read() throws IOException {
        lastChar = readInternal();

        return lastChar;
    }

    /**
     * Returns the last read character.
     * 
     * @return the last read character or -1 for an eof.
     */
    public int getLastChar() {
        return lastChar;
    }

    // The implementation of the read method.
    private int readInternal() throws IOException {
        int i;
        label0: {
            pos = chpos;
            chpos++;
            i = pushBack;

            if ( i == -1) {
                if ( currentIndex >= numChars) {
                    numChars = in.read( buffer);

                    if ( numChars == -1) {
                        i = -1;
                        break label0;
                    }

                    currentIndex = 0;
                }
                i = buffer[currentIndex++];
            } else {
                pushBack = -1;
            }
        }

        switch ( i) {
        case 10: // '\n'
            chpos += 0x100000000L;
            return 10;

        case 13: // '\r'
            if ( (i = getNextChar()) != 10)
                pushBack = i;
            else
                chpos++;
            chpos += 0x100000000L;
            return 10;
        }

        return i;
    }

    // Returns the next character from the stream
    private int getNextChar() throws IOException {
        if ( currentIndex >= numChars) {
            numChars = in.read( buffer);

            if ( numChars == -1) {
                return -1;
            }

            currentIndex = 0;
        }

        return buffer[currentIndex++];
    }

    /**
     * Checks if sequence read from this reader starts with specified matching string
     * 
     * Added by Vladimir Nikic, May 22nd, 2007.
     * 
     * @param matchingString
     * @return True if starts with matching string, otherwise false
     */
    public boolean startsWith(String matchingString) throws IOException {
        if (matchingString == null) {
            return false;
        }

        if (matchingString.length() == 0) {
            return true;
        }
        
        int len = matchingString.length();
        int index = 0;
        while (index < len) {
            int ch = this.read();
            if ( ch == -1 || matchingString.charAt(index) != ch) {
                return false;
            }
            index++;
        }

        return true;
    }

    /**
     * Checks if sequence consisting of specified first character and following
     * characters read from this reader starts with specified matching string
     *
     * Added by Vladimir Nikic, May 22nd, 2007.
     *
     * @param firstChar
     * @param matchingString
     * @return True if starts with matching string, otherwise false
     */
    public boolean startsWith(int firstChar, String matchingString) throws IOException {
        if (matchingString == null || matchingString.length() == 0) {
            return false;
        }

        return matchingString != null &&
               matchingString.length() > 0 &&
               matchingString.charAt(0) == firstChar &&
               startsWith( matchingString.substring(1) ); 
    }

    public void unread(int num) {
        if (this.currentIndex > num) {
            this.currentIndex -= num;
            this.lastChar = buffer[currentIndex];
        }
    }

}
