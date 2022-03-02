/*
 * $Id: XMLEditorKit.java,v 1.4 2005/03/28 15:20:26 edankert Exp $
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

import org.webharvest.gui.*;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
 * The XML editor kit supports handling of editing XML content. It supports
 * syntax highlighting, line wrapping, automatic indentation and tag completion.
 * <p>
 * <b>Note: </b> The XML Editor package is based on the JavaEditorKit example as
 * described in the article <i>'Customizing a Text Editor' </i> by <b>Timothy
 * Prinzing </b>. See:
 * http://java.sun.com/products/jfc/tsc/articles/text/editor_kit/
 * </p>
 * <pre><code>
 * JEditorPane editor = new JEditorPane(); 
 * 
 * // Instantiate a XMLEditorKit with wrapping enabled.
 * XMLEditorKit kit = new XMLEditorKit( true); 
 * 
 * // Set the wrapping style.
 * kit.setWrapStyleWord( true);
 * 
 * editor.setEditorKit( kit); 
 *
 * // Set the font style.
 * editor.setFont( new Font( "Courier", Font.PLAIN, 12)); 
 * 
 * // Set the tab size
 * editor.getDocument().putProperty( PlainDocument.tabSizeAttribute, new Integer(4));
 * 
 * // Enable auto indentation.
 * editor.getDocument().putProperty( XMLDocument.AUTO_INDENTATION_ATTRIBUTE, new Boolean( true));
 * 
 * // Enable tag completion.
 * editor.getDocument().putProperty( XMLDocument.TAG_COMPLETION_ATTRIBUTE, new Boolean( true));
 * 
 * // Set a style
 * kit.setStyle( XMLStyleConstants.ATTRIBUTE_NAME, new Color( 255, 0, 0), Font.BOLD);
 * 
 * // Put the editor in a panel that will force it to resize, when a different view is choosen.
 * ScrollableEditorPanel editorPanel = new ScrollableEditorPanel( editor);
 * 
 * JScrollPane scroller = new JScrollPane( editorPanel);
 * 
 * ...
 * </code></pre>
 * <p>
 * To switch between line wrapped and non wrapped views use:
 * </p>
 * <pre><code>
 * ...
 * 
 * XMLEditorKit kit = (XMLEditorKit)editor.getEditorKit();
 * kit.setLineWrappingEnabled( false);
 * 
 * // Update the UI and create a new view...
 * editor.updateUI();
 * 
 * ...
 * </code></pre>
 * 
 * @version $Revision: 1.4 $, $Date: 2005/03/28 15:20:26 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class XMLEditorKit extends DefaultEditorKit implements XMLStyleConstants {
    private static final long serialVersionUID = 3832623997442667569L;

    private XMLContext context  = null;
    private ViewFactory factory = null;
    private JEditorPane editor = null;

    private boolean lineWrapping = false;
    private boolean wrapStyleWord = false;

    private XmlTextPane xmlTextPane;

    /**
     * Called when the kit is being installed into the
     * a JEditorPane.  
     *
     * @param c the JEditorPane
     */
    public void install(JEditorPane c) {
        super.install( c);

        this.editor = c;
    }

    /**
     * Constructs the view factory and the Context.
     * 
     * @param lineWrapping enables line wrapping feature if true.
     */
    public XMLEditorKit( boolean lineWrapping, XmlTextPane xmlTextPane) {
        super();

        this.xmlTextPane = xmlTextPane;
        
        factory = new XMLViewFactory();
        context = new XMLContext();
        
        this.lineWrapping = lineWrapping;
    }
    
    /**
     * Returns true when line-wrapping has been turned on.
     * 
     * @return state of line-wrapping feature.
     */
    public boolean isLineWrapping() {
        return lineWrapping;
    }

    /**
     * Eanbles/disables the line-wrapping feature.
     * 
     * @param enabled true when line-wrapping enabled.
     */
    public void setLineWrappingEnabled( boolean enabled) {
        lineWrapping = enabled;
    }

    /**
     * Returns true when the wrapping style is 'word wrapping'.
     * 
     * @return the style of wrapping.
     */
    public boolean isWrapStyleWord() {
        return wrapStyleWord;
    }

    /**
     * Enables/disables the word-wrapping style.
     * 
     * @param enabled true when word-wrapping style enabled.
     */
    public void setWrapStyleWord( boolean enabled) {
        wrapStyleWord = enabled;
    }

    /**
     * Get the MIME type of the data that this kit represents support for. This
     * kit supports the type <code>text/xml</code>.
     * 
     * @return the type.
     */
    public String getContentType() {
        return "text/xml";
    }

    /**
     * Fetches the XML factory that can produce views for XML Documents.
     * 
     * @return the XML factory
     */
    public final ViewFactory getViewFactory() {
        return factory;
    }
    
    /**
     * Set the style identified by the name.
     * 
     * @param name the style name
     * @param foreground the foreground color
     * @param fontStyle the font style Plain, Italic or Bold
     */
    public void setStyle( String name, Color foreground, int fontStyle) {
        context.setStyle( name, foreground, fontStyle);
    }
    
    /**
     * @see DefaultEditorKit#createDefaultDocument()
     */
    public Document createDefaultDocument() {
        return new XMLDocument( editor);
    }
    
    /**
     * @see DefaultEditorKit#read( java.io.Reader, javax.swing.text.Document, int)
     */
    public void read( Reader in, Document doc, int pos) throws IOException, BadLocationException {
        doc.putProperty( XMLDocument.LOADING_ATTRIBUTE, Boolean.TRUE);
        
        super.read( in, doc, pos);

        doc.putProperty( XMLDocument.LOADING_ATTRIBUTE, Boolean.FALSE);
    }

    /**
     * @see DefaultEditorKit#read( java.io.InputStream, javax.swing.text.Document, int)
     */
    public void read( InputStream in, Document doc, int pos) throws IOException, BadLocationException {
        doc.putProperty( XMLDocument.LOADING_ATTRIBUTE, Boolean.TRUE);
        
        super.read( in, doc, pos);

        doc.putProperty( XMLDocument.LOADING_ATTRIBUTE, Boolean.FALSE);
    }

    /**
     * A simple view factory implementation.
     */
    class XMLViewFactory implements ViewFactory {
        /**
         * Creates the XML View.
         * 
         * @param elem the root element.
         * @return the XMLView
         */
        public View create( Element elem) {
            if ( lineWrapping) {
                try {
                    return new WrappedXMLView( context, elem, wrapStyleWord);
                } catch ( IOException e) {
                    // Instead of an IOException, this will return null if the 
                    // WrappedXMLView could not be instantiated.
                    // Should never happen.
                }
            } else {
                try {
                    return new XMLView(context, elem, xmlTextPane);
                } catch ( IOException e) {
                    // Instead of an IOException, this will return null if the 
                    // XMLView could not be instantiated. 
                    // Should never happen.
                }
            }

            return null;
        }
    }
}