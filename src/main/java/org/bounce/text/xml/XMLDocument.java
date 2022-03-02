/*
 * $Id: XMLDocument.java,v 1.2 2005/03/28 13:46:07 edankert Exp $
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

import javax.swing.JEditorPane;
import javax.swing.text.*;

/**
 * The XML Document is responsible for handling the user insertions and
 * deletions, for changing the tab characters to spaces and to automatically
 * indent the text correctly.
 * 
 * <p>
 * <b>Note: </b> The XML Editor package is based on the JavaEditorKit example as
 * described in the article <i>'Customizing a Text Editor' </i> by <b>Timothy
 * Prinzing </b>. See:
 * http://java.sun.com/products/jfc/tsc/articles/text/editor_kit/
 * </p>
 * 
 * @version $Revision: 1.2 $, $Date: 2005/03/28 13:46:07 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class XMLDocument extends PlainDocument {
    private static final long serialVersionUID = 3256723974510424372L;

    /**
     * Name of the attribute that specifies whether tag-completion is enabled. 
     * The type for the value is Boolean.
     */
    public final static String TAG_COMPLETION_ATTRIBUTE   = "tagCompletion";

    /**
     * Name of the attribute that specifies whether auto indentation is enabled.
     * The type for the value is Boolean.
     */
    public final static String AUTO_INDENTATION_ATTRIBUTE = "autoIndentation";

    /**
     * While loading information, no tag-completion or 
     * auto-indentation should take place.
     */
    final static String LOADING_ATTRIBUTE           = "loading";
    
    private JEditorPane editor = null;

    /**
     * Constructs the XML Document with a new GapContent buffer.
     * 
     * @param editor the editor component.
     */
    public XMLDocument( JEditorPane editor) {
        super( new GapContent( 1024));
        
        this.editor = editor;
    }
    
    private boolean isLoading() {
        Boolean loading = (Boolean)getProperty( LOADING_ATTRIBUTE);
        
        if ( loading != null) {
            return loading.booleanValue();
        }
        
        return false;
    }
    
    /**
     * Returns the status of the auto indentation.
     * 
     * @return true when auto indentation is enabled.
     */
    private boolean isAutoIndentation() {
        Boolean autoIndentation = (Boolean)getProperty( AUTO_INDENTATION_ATTRIBUTE);
        
        if ( autoIndentation != null && !isLoading()) {
            return autoIndentation.booleanValue();
        }
        
        return false;
    }
    
    /**
     * Returns the status of the tag completion.
     * 
     * @return true when tag completion is enabled.
     */
    private boolean isTagCompletion() { 
        Boolean tagCompletion = (Boolean)getProperty( TAG_COMPLETION_ATTRIBUTE);
        
        if ( tagCompletion != null && !isLoading()) {
            return tagCompletion.booleanValue();
        }
        
        return false;
    }

    /**
     * Inserts some content into the document. When the content is a '>' 
     * character and it is the end of a start-tag and auto tag completion 
     * has been enabled, a new end-tag will be created.
     * When the content is a new line character and auto indentation has 
     * been enabled, an indentation will be added to the content.
     * 
     * @see PlainDocument#insertString( int, String, AttributeSet)
     */
    public void insertString( int off, String str, AttributeSet set) throws BadLocationException {
        if ( str.equals( ">") && isTagCompletion()) {
            int dot = editor.getCaret().getDot();
            
            StringBuffer endTag = new StringBuffer( str);
    
            String text = getText( 0, off);
            int startTag = text.lastIndexOf( '<', off);
            int prefEndTag = text.lastIndexOf( '>', off);
    
            // If there was a start tag and if the start tag is not empty
            // and
            // if the start-tag has not got an end-tag already.
            if ( (startTag > 0) && (startTag > prefEndTag) && (startTag < text.length() - 1)) {
                String tag = text.substring( startTag, text.length());
                char first = tag.charAt( 1);
    
                if ( first != '/' && first != '!' && first != '?' && !Character.isWhitespace( first)) {
                    boolean finished = false;
                    char previous = tag.charAt( tag.length() - 1);
    
                    if ( previous != '/' && previous != '-') {
    
                        endTag.append( "</");
    
                        for ( int i = 1; (i < tag.length()) && !finished; i++) {
                            char ch = tag.charAt( i);
    
                            if ( !Character.isWhitespace( ch)) {
                                endTag.append( ch);
                            } else {
                                finished = true;
                            }
                        }
    
                        endTag.append( ">");
                    }
                }
            }
    
            str = endTag.toString();
    
            super.insertString( off, str, set);
    
            editor.getCaret().setDot( dot + 1);
        } else if ( str.equals( "\n") && isAutoIndentation()) {
            StringBuffer newStr = new StringBuffer(str);
            Element elem = getDefaultRootElement().getElement( getDefaultRootElement().getElementIndex(off));
            int start = elem.getStartOffset();
            int end = elem.getEndOffset();
            String line = getText( start, off - start);

            boolean finished = false;

            for ( int i = 0; (i < line.length()) && !finished; i++) {
                char ch = line.charAt( i);

                if ( ((ch != '\n') && (ch != '\f') && (ch != '\r')) && Character.isWhitespace( ch)) {
                    newStr.append(ch);
                } else {
                    finished = true;
                }
            }

            String elementName = XmlParserUtils.getStartElement(line);
            if (elementName != null) {
                String remainingText = getText(off, end - off - 1);
                // if text on the right is not closing tag, then indent text in the following line
                if ( remainingText == null || !remainingText.trim().startsWith("</" + elementName + ">") ) {
                    newStr.append( "\t");
                }
            }

            str = newStr.toString();

            super.insertString( off, str, set);
        } else {
            super.insertString( off, str, set);
        }
    }

}