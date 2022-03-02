/*
 * $Id: WrappedXMLView.java,v 1.1 2005/03/28 13:34:47 edankert Exp $
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

import java.awt.Graphics;
import java.awt.Shape;
import java.io.IOException;

import javax.swing.text.View;
import javax.swing.text.Element;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.WrappedPlainView;

/**
 * The XML View uses the XML scanner to determine the style (font, color) of the
 * text that it renders.
 * <p>
 * <b>Note: </b> The XML Editor package is based on the JavaEditorKit example as
 * described in the article <i>'Customizing a Text Editor' </i> by <b>Timothy
 * Prinzing </b>. See:
 * http://java.sun.com/products/jfc/tsc/articles/text/editor_kit/
 * </p>
 * 
 * @version $Revision: 1.1 $, $Date: 2005/03/28 13:34:47 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class WrappedXMLView extends WrappedPlainView {

    private XMLScanner scanner = null;
    private XMLContext context = null;

    /**
     * Construct a colorized view of xml text for the element. Gets the current
     * document and creates a new Scanner object.
     * 
     * @param context the styles used to colorize the view.
     * @param elem the element to create the view for.
     * 
     * @throws IOException
     */
    public WrappedXMLView( XMLContext context, Element elem) throws IOException {
        this( context, elem, false);
    }

    /**
     * Construct a colorized view of xml text for the element. Gets the current
     * document and creates a new Scanner object.
     * 
     * @param context the styles used to colorize the view.
     * @param elem the element to create the view for.
     * @param wrapStyleWord the word wrapping-style.
     * 
     * @throws IOException
     */
    public WrappedXMLView( XMLContext context, Element elem, boolean wrapStyleWord) throws IOException {
        super( elem, wrapStyleWord);

        this.context = context;
        Document doc = getDocument();

        scanner = new XMLScanner( doc);
    }

    /**
     * Invalidates the scanner, to make sure a new range is set later.
     * 
     * @see View#paint( Graphics g, Shape a)
     * 
     * @param g the graphics context.
     * @param a the shape.
     */
    public void paint( Graphics g, Shape a) {
        super.paint( g, a);

        scanner.setValid( false);
    }

    /**
     * Renders the given range in the model as normal unselected text. This will
     * paint the text according to the XMLContext styles..
     * 
     * @param g the graphics context
     * @param x the starting X coordinate
     * @param y the starting Y coordinate
     * @param start the beginning position in the model
     * @param end the ending position in the model
     * 
     * @return the location of the end of the range
     * 
     * @exception BadLocationException if the range is invalid
     */
    protected int drawUnselectedText( Graphics g, int x, int y, int start, int end) throws BadLocationException {
        return XMLViewUtilities.drawUnselectedText( this, scanner, context, g, x, y, start, end);
    }

    /**
     * Renders the given range in the model as selected text. This will paint
     * the text according to the font as found in the XMLContext styles..
     * 
     * @param g the graphics context
     * @param x the starting X coordinate
     * @param y the starting Y coordinate
     * @param start the beginning position in the model
     * @param end the ending position in the model
     * 
     * @return the location of the end of the range
     * 
     * @exception BadLocationException if the range is invalid
     */
    protected int drawSelectedText( Graphics g, int x, int y, int start, int end) throws BadLocationException {
        return XMLViewUtilities.drawSelectedText( this, scanner, context, g, x, y, start, end);
    }
}
