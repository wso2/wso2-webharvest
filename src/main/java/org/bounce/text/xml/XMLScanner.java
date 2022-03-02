/*
 * $Id: XMLScanner.java,v 1.2 2005/03/28 13:34:48 edankert Exp $
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

import java.io.IOException;

import javax.swing.text.Document;

/**
 * Associates XML input stream characters with XML specific styles.
 * <p>
 * <b>Note:</b> The XML Editor package is based on the JavaEditorKit example as
 * described in the article <i>'Customizing a Text Editor' </i> by <b>Timothy
 * Prinzing </b>. See:
 * http://java.sun.com/products/jfc/tsc/articles/text/editor_kit/
 * </p>
 * 
 * @version $Revision: 1.2 $, $Date: 2005/03/28 13:34:48 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class XMLScanner {
    private Scanner tagScanner = null;

    private final AttributeScanner ATTRIBUTE_SCANNER = new AttributeScanner();
    private final ElementEndTagScanner ELEMENT_END_TAG_SCANNER = new ElementEndTagScanner();
    private final ElementStartTagScanner ELEMENT_START_TAG_SCANNER = new ElementStartTagScanner();
    private final ElementNameScanner ELEMENT_NAME_SCANNER = new ElementNameScanner();
    private final EntityTagScanner ENTITY_TAG_SCANNER = new EntityTagScanner();
    private final CommentScanner COMMENT_SCANNER = new CommentScanner();
    private final CDataScanner CDATA_SCANNER = new CDataScanner();
    private final TagScanner TAG_SCANNER = new TagScanner();

    private int start = 0;
    private long pos = 0;

    private XMLInputReader in = null;
    
    private boolean valid = false;
    
    /** The last token scanned */
    public String token = null;
    
    /**
     * Constructs a scanner for the Document.
     * 
     * @param document the document containing the XML content.
     * 
     * @throws IOException
     */
    public XMLScanner( Document document) throws IOException {
        try {
            in = new XMLInputReader( new XMLInputStream( document));
        } catch ( Exception exception) {
            exception.printStackTrace();
        }

        in.read();
        scan();
    }
    
    /**
     * Returns true when no paint has invalidated the scanner.
     * 
     * @return true when no paint has invalidated the output.
     */
    boolean isValid() {
        return valid;
    }
    
    /**
     * Set valid when correct range is set.
     * 
     * @param valid when correct range set.
     */
    void setValid( boolean valid) {
        this.valid = valid;
    }

    /**
     * Sets the scanning range.
     * 
     * @param start the start of the range.
     * @param end the end of the range.
     * 
     * @throws IOException
     */
    public void setRange( int start, int end) throws IOException {
        in.setRange( start, end);

        this.start = start;

        token = null;
        pos = 0;
        tagScanner = null;

        in.read();
        scan();
    }

    /**
     * Gets the starting location of the current token in the document.
     * 
     * @return the starting location.
     */
    public final int getStartOffset() {
        int begOffs = (int) pos;
        return start + begOffs;
    }

    /**
     * Gets the end location of the current token in the document.
     * 
     * @return the end location.
     */
    public final int getEndOffset() {
        int endOffs = (int) in.pos;
        return start + endOffs;
    }

    /**
     * Scans the Xml Stream for XML specific tokens.
     * 
     * @return the last location.
     * 
     * @throws IOException
     */
    public long scan() throws IOException {
        long l = pos;

        if ( tagScanner != null) {
            token = tagScanner.scan( in);

            if ( tagScanner.isFinished()) {
                tagScanner = null;
            }

            return l;
        } else {

            while ( true) {
                pos = in.pos;
                int ch = in.getLastChar();

                switch ( ch) {
                case -1:
                    token = null;
                    return l;

                case 60: // '<'
                    ch = in.read();

                    tagScanner = TAG_SCANNER;
                    tagScanner.reset();

                    token = tagScanner.scan( in);
                    return l;

                default:
                    scanValue();
                    token = XMLStyleConstants.ELEMENT_VALUE;
                    return l;
                }
            }
        }
    }

    // Scans a XML element value.
    private void scanValue() throws IOException {
        int ch = in.read();

        do {
            switch ( ch) {
            case -1:
                // eof
                return;

            case 60: // '<'
                return;

            default:
                ch = in.read();
                break;

            }
        } while ( true);
    }

    // Returns when a non whitespace character has been detected.
    private void skipWhitespace() throws IOException {
        int ch = in.read();
        // int ch = in.getLastChar();

        while ( true) {
            if ( Character.isWhitespace( (char) ch)) {
                ch = in.read();
            } else {
                return;
            }
        }
    }

    // Scans a String.
    private void scanString( int end) throws IOException {
        int ch = in.read();

        while ( ch != end && ch != '>' && ch != -1) {
            ch = in.read();
        }
    }

    /**
     * A scanner for anything starting with a ' <'.
     */
    private class TagScanner extends Scanner {
        private Scanner scanner = null;

        /**
         * @see Scanner#scan(XMLInputReader)
         */
        public String scan( XMLInputReader in) throws IOException {
            if ( scanner != null) {
                String token = scanner.scan( in);

                if ( scanner.isFinished()) {
                    scanner = null;
                }

                return token;
            } else {
                int character = in.getLastChar();

                if ( character == 33) { // '!'
                    character = in.read();
                    if ( character == 45) { // '-'
                        character = in.read();
                        if ( character == 45) { // '-'
                            scanner = COMMENT_SCANNER;
                            character = in.read();
                        }
                    } else {
                        boolean isCDataStart = in.startsWith(character, "[CDATA[");
                        character = in.getLastChar();
                        if (isCDataStart) {
                            scanner = CDATA_SCANNER;
                            character = in.read();
                        }
                    }

                    if ( scanner == null) {
                        scanner = ENTITY_TAG_SCANNER;
                    }

                    scanner.reset();
                    return XMLStyleConstants.SPECIAL;

                } else if ( character == 63) { // '?'
                    character = in.read();
                    scanner = ENTITY_TAG_SCANNER;
                    scanner.reset();

                    return XMLStyleConstants.SPECIAL;

                } else if ( character == 47) { // '/'
                    character = in.read();
                    scanner = ELEMENT_END_TAG_SCANNER;
                    scanner.reset();

                    return XMLStyleConstants.SPECIAL;

                } else if ( character == 62) { // '>'
                    character = in.read();
                    setFinished();
                    return XMLStyleConstants.SPECIAL;

                } else {
                    scanner = ELEMENT_START_TAG_SCANNER;
                    scanner.reset();

                    return XMLStyleConstants.SPECIAL;
                }
            }
        }

        /**
         * @see Scanner#reset()
         */
        public void reset() {
            super.reset();
            scanner = null;
        }
    }

    /**
     * Scans a entity ' <!'.
     */
    private class EntityTagScanner extends Scanner {

        /**
         * @see Scanner#scan(XMLInputReader)
         */
        public String scan( XMLInputReader in) throws IOException {
            int character = in.read();

            while ( true) {
                switch ( character) {
                case -1:
                    // System.err.println("Error ["+pos+"]: eof in entity!");
                    setFinished();
                    return XMLStyleConstants.ENTITY;

                case 62: // '>'
                    setFinished();
                    return XMLStyleConstants.ENTITY;

                default:
                    character = in.read();
                    break;

                }
            }
        }

        /**
         * @see Scanner#reset()
         */
        public void reset() {
            super.reset();
        }
    }

    /**
     * Scans a comment entity ' <!--'.
     */
    private class CommentScanner extends Scanner {
        /**
         * @see Scanner#scan(XMLInputReader)
         */
        public String scan(XMLInputReader in) throws IOException {
            int character = in.read();

            while (true) {
                // System.out.print((char)character);

                switch ( character) {
                case -1: // EOF
                    setFinished();
                    return XMLStyleConstants.COMMENT;

                case 45: // '-'
                    boolean isCommentEnd = in.startsWith("->");
                    character = in.getLastChar();
                    if (isCommentEnd) {
                        setFinished();
                        return XMLStyleConstants.COMMENT;
                    }
                    break;
                default:
                    character = in.read();
                    break;

                }
            }
        }

        /**
         * @see Scanner#reset()
         */
        public void reset() {
            super.reset();
        }
    }

    /**
     * Scans a cdata section '<![CDATA'.
     */
    private class CDataScanner extends Scanner {
        public String scan(XMLInputReader in) throws IOException {
            int character = in.read();

            while (true) {
                switch (character) {
                    case -1:
                        setFinished();
                        return XMLStyleConstants.CDATA;
                    case ']':
                        boolean isCDataEnd = in.startsWith("]>");
                        if (isCDataEnd) {
                            setFinished();
                            return XMLStyleConstants.CDATA;
                        }
                        character = in.getLastChar();
                        break;
                    default:
                        character = in.read();
                        break;
                }
            }
        }

        public void reset() {
            super.reset();
        }
    }

    /**
     * Scans an element end tag ' </xxx:xxxx>'.
     */
    private class ElementEndTagScanner extends Scanner {
        private Scanner scanner = null;

        /**
         * @see Scanner#scan(XMLInputReader)
         */
        public String scan( XMLInputReader in) throws IOException {
            // System.out.println( "ElementStartTagScanner.scan()");
            if ( scanner == null) {
                scanner = ELEMENT_NAME_SCANNER;
                scanner.reset();
            }

            String token = scanner.scan( in);

            if ( scanner.isFinished()) {
                setFinished();
            }

            return token;
        }

        /**
         * @see Scanner#reset()
         */
        public void reset() {
            super.reset();
            scanner = null;
        }
    }

    /**
     * Scans an element start tag ' <xxx:xxxx yyy:yyyy="yyyyy"
     * xmlns:hsshhs="sffsfsf">'.
     */
    private class ElementStartTagScanner extends Scanner {
        private Scanner scanner = null;

        /**
         * @see Scanner#scan(XMLInputReader)
         */
        public String scan( XMLInputReader in) throws IOException {
            String token = null;

            if ( scanner == null) {
                scanner = ELEMENT_NAME_SCANNER;
                scanner.reset();

                token = scanner.scan( in);
            } else {
                token = scanner.scan( in);
            }

            if ( scanner.isFinished()) {
                if ( scanner instanceof ElementNameScanner) {
                    scanner = ATTRIBUTE_SCANNER;
                    scanner.reset();
                } else {
                    setFinished();
                }
            }

            return token;
        }

        /**
         * @see Scanner#reset()
         */
        public void reset() {
            super.reset();
            scanner = null;
        }
    }

    /**
     * Scans an element name ' <xxx:xxxx'.
     */
    private class ElementNameScanner extends Scanner {
        private boolean hasPrefix = false;

        private boolean emptyElement = false;

        /**
         * @see Scanner#scan(XMLInputReader)
         */
        public String scan( XMLInputReader in) throws IOException {

            int character = in.getLastChar();

            do {
                switch ( character) {
                case -1:
                    // System.err.println("Error ["+pos+"]: eof in element
                    // name!");
                    setFinished();
                    return XMLStyleConstants.ELEMENT_NAME;

                case 58: // ':'
                    if ( hasPrefix) {
                        character = in.read();
                        return XMLStyleConstants.SPECIAL;
                    } else {
                        hasPrefix = true;
                        return XMLStyleConstants.ELEMENT_PREFIX;
                    }

                case 47: // '/'
                    if ( emptyElement) {
                        character = in.read();
                    } else {
                        emptyElement = true;
                        return XMLStyleConstants.ELEMENT_NAME;
                    }

                case 62: // '>'
                    setFinished();

                    if ( emptyElement) {
                        return XMLStyleConstants.SPECIAL;
                    } else {
                        return XMLStyleConstants.ELEMENT_NAME;
                    }

                case 32: // ' '
                case 10: // '\r'
                case 13: // '\n'
                    skipWhitespace();
                    setFinished();
                    return XMLStyleConstants.ELEMENT_NAME;

                default:
                    character = in.read();
                    break;

                }
            } while ( true);
        }

        /**
         * @see Scanner#reset()
         */
        public void reset() {
            super.reset();
            emptyElement = false;
            hasPrefix = false;
        }
    }

    /**
     * Scans an elements attribute 'xxx:xxxx="hhhh"' or 'xmlns:xxxx="hhhh"'.
     */
    private class AttributeScanner extends Scanner {
        private final int NAME = 0;

        private final int VALUE = 1;

        private final int END = 2;

        private int mode = NAME;

        private boolean hasPrefix = false;

        private boolean firstTime = true;

        private boolean isNamespace = false;

        /**
         * @see Scanner#scan(XMLInputReader)
         */
        public String scan( XMLInputReader in) throws IOException {

            int character = in.getLastChar();

            // System.out.println("AttributeScanner.scan()
            // ["+(char)character+"]");

            do {
                if ( mode == NAME) {
                    // System.out.println("NAME ["+(char)character+"]
                    // "+firstTime);

                    switch ( character) {
                    case -1:
                        // System.err.println("Error ["+pos+"]: eof in
                        // attribute!");
                        setFinished();
                        return XMLStyleConstants.ATTRIBUTE_NAME;

                    case 120: // 'x'
                        if ( firstTime) { // Still before a prefix has been
                            // established
                            character = in.read();
                            if ( character == 109) { // 'm'
                                character = in.read();

                                if ( character == 108) { // 'l'
                                    character = in.read();

                                    if ( character == 110) { // 'n'

                                        character = in.read();
                                        if ( character == 115) { // 's'
                                            skipWhitespace();
                                            character = in.getLastChar();

                                            if ( character == 58 || character == 61) { // ':'
                                                // '='
                                                isNamespace = true;
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            character = in.read();
                        }
                        break;

                    case 58: // ':'
                        if ( hasPrefix) {
                            character = in.read();
                            return XMLStyleConstants.SPECIAL;
                        } else if ( isNamespace) {
                            hasPrefix = true;
                            return XMLStyleConstants.NAMESPACE_NAME;
                        } else {
                            hasPrefix = true;
                            return XMLStyleConstants.ATTRIBUTE_PREFIX;
                        }

                    case 62: // '>'
                        // character = in.read();
                        setFinished();
                        return XMLStyleConstants.SPECIAL;

                    case 61: // '='
                        mode = VALUE;

                        if ( isNamespace && hasPrefix) {
                            return XMLStyleConstants.NAMESPACE_PREFIX;
                        } else if ( isNamespace) {
                            return XMLStyleConstants.NAMESPACE_NAME;
                        } else {
                            return XMLStyleConstants.ATTRIBUTE_NAME;
                        }

                    default:
                        character = in.read();
                        break;
                    }

                    firstTime = false;
                } else if ( mode == VALUE) {

                    // System.out.println("VALUE ["+(char)character+"]");

                    switch ( character) {
                    case -1:
                        // System.err.println("Error ["+pos+"]: eof in attribute
                        // value!");
                        return null;

                    case 61: // '='
                        character = in.read();
                        return XMLStyleConstants.SPECIAL;

                    case 39: // '''
                    case 34: // '"'
                        scanString( character);
                        skipWhitespace();

                        if ( isNamespace) {
                            reset();
                            return XMLStyleConstants.NAMESPACE_VALUE;
                        } else {
                            reset();
                            return XMLStyleConstants.ATTRIBUTE_VALUE;
                        }

                    case 62: // '>'
                        character = in.read();
                        setFinished();
                        return XMLStyleConstants.SPECIAL;

                    default:
                        character = in.read();
                        break;
                    }

                }
            } while ( true);
        }

        /**
         * @see Scanner#reset()
         */
        public void reset() {
            super.reset();
            mode = NAME;

            hasPrefix = false;
            firstTime = true;
            isNamespace = false;
        }
    }

    /**
     * Abstract scanner class..
     */
    abstract class Scanner {
        protected int token = -1;

        private boolean finished = false;

        /**
         * Scan the input steam for a token.
         * 
         * @param in the input stream reader.
         * @return the token.
         * @throws IOException
         */
        public abstract String scan( XMLInputReader in) throws IOException;

        protected void setFinished(boolean isFinished) {
            finished = isFinished;
        }

        /**
         * The scanner has finished scanning the information, only a reset can
         * change this.
         */
        protected void setFinished() {
            setFinished(true);
        }

        /**
         * returns whether this scanner has finished scanning all it was
         * supposed to scan.
         * 
         * @return true when the scanner is finished.
         */
        public boolean isFinished() {
            return finished;
        }

        /**
         * Resets all the variables to the start value.
         */
        public void reset() {
            finished = false;
            token = -1;
        }

        /**
         * returns the token value for the currently scanned text.
         * 
         * @return the token value.
         */
        public int getToken() {
            return token;
        }
    }
}
