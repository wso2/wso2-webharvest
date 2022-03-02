/*
 * $Id: XMLViewUtilities.java,v 1.1 2005/03/28 13:34:48 edankert Exp $
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
import java.awt.Color;

import javax.swing.text.TabExpander;
import javax.swing.text.View;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.Segment;
import javax.swing.text.Utilities;
import javax.swing.*;

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
 * @version $Revision: 1.1 $, $Date: 2005/03/28 13:34:48 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
class XMLViewUtilities { 
    private static Segment lineBuffer = null;
    
    /**
     * Renders the given range in the model as normal unselected text. This will
     * paint the text according to the styles..
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
    static int drawUnselectedText( View view, XMLScanner scanner, XMLContext context, Graphics g, int x, int y, int start, int end) throws BadLocationException {
        Document doc = view.getDocument();
        Style lastStyle = null;
        String lastToken = null;
        int mark = start;

        Color forceColor = null;
        if (view instanceof XMLView) {
            Color bgPaintColor = ((XMLView)view).getBgPaintColor();
            if (bgPaintColor != null) {
                if ( bgPaintColor.equals(XMLView.ERROR_COLOR) || bgPaintColor.equals(XMLView.DEBUG_STOP_COLOR) ) {
                    forceColor = Color.white;
                }
            }
        }

        while ( start < end) {
            updateScanner( scanner, doc, start);

            int p = Math.min( scanner.getEndOffset(), end);
            p = (p <= start) ? end : p;

            Style style = context.getStyle( scanner.token);

            // If the style changes, do paint...
            if ( style != lastStyle && lastStyle != null) {
                // color change, flush what we have
                g.setColor( forceColor != null ? forceColor : context.getForeground( lastStyle));
                g.setFont( g.getFont().deriveFont( context.getFontStyle( lastStyle)));

                Segment text = getLineBuffer();
                doc.getText( mark, start - mark, text);

                x = Utilities.drawTabbedText( text, x, y, g, (TabExpander)view, mark);
                mark = start;
            }

            lastToken = scanner.token;
            lastStyle = style;
            start = p;
        }

        // flush remaining
        g.setColor( forceColor != null ? forceColor : context.getForeground( lastStyle));
        g.setFont( g.getFont().deriveFont( context.getFontStyle( lastStyle)));
        Segment text = getLineBuffer();
        doc.getText( mark, end - mark, text);

        x = Utilities.drawTabbedText( text, x, y, g, (TabExpander)view, mark);

        return x;
    }

    /**
     * Renders the given range in the model as selected text. This will paint
     * the text according to the font as found in the styles..
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
    static int drawSelectedText( View view, XMLScanner scanner, XMLContext context, Graphics g, int x, int y, int start, int end) throws BadLocationException {
        Document doc = view.getDocument();
        Style lastStyle = null;
        String lastToken = null;
        int mark = start;

        g.setColor(Color.white);

        while ( start < end) {
            updateScanner( scanner, doc, start);

            int p = Math.min( scanner.getEndOffset(), end);
            p = (p <= start) ? end : p;

            Style style = context.getStyle( scanner.token);

            // If the style changes, do paint...
            if ( style != lastStyle && lastStyle != null) {
                // color change, flush what we have
                g.setFont( g.getFont().deriveFont( context.getFontStyle( lastStyle)));

                Segment text = getLineBuffer();
                doc.getText( mark, start - mark, text);

                x = Utilities.drawTabbedText( text, x, y, g, (TabExpander)view, mark);
                mark = start;
            }

            lastToken = scanner.token;
            lastStyle = style;
            start = p;
        }

        // flush remaining
        g.setFont( g.getFont().deriveFont( context.getFontStyle( lastStyle)));
        Segment text = getLineBuffer();
        doc.getText( mark, end - mark, text);

        x = Utilities.drawTabbedText( text, x, y, g, (TabExpander)view, mark);

        return x;
    }

    // Update the scanner to point to the '<' begin token.
    private static void updateScanner( XMLScanner scanner, Document doc, int p) {
        try {
            if ( !scanner.isValid()) {
                scanner.setRange( getTagEnd( doc, p), doc.getLength());
                scanner.setValid( true);
            }

            while ( scanner.getEndOffset() <= p) {
                scanner.scan();
            }
        } catch ( Throwable e) {
            // can't adjust scanner... calling logic
            // will simply render the remaining text.
            e.printStackTrace();
        }
    }

    // Return the end position of the current tag.
    private static int getTagEnd( Document doc, int p) {
        int elementEnd = 0;

        if ( p > 0) {
            try {
                int index = 0;

                String s = doc.getText( 0, p);
                int commentStart = s.lastIndexOf( "<!--");
                int commentEnd = s.lastIndexOf( "-->");

                if ( commentStart > 0 && commentStart > commentEnd) {
                    index = s.lastIndexOf( ">", commentStart);
                } else {
                    index = s.lastIndexOf( ">");
                }

                if ( index != -1) {
                    elementEnd = index;
                }
            } catch ( BadLocationException bl) {
            }
        }

        return elementEnd;
    }
    
    private static Segment getLineBuffer() {
        if (lineBuffer == null) {
            lineBuffer = new Segment();
        }
        return lineBuffer;
    }
}
