/*
 * $Id: XMLView.java,v 1.2 2005/03/28 13:34:48 edankert Exp $
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

import java.awt.*;
import java.io.IOException;

import javax.swing.text.*;
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
 * @version $Revision: 1.2 $, $Date: 2005/03/28 13:34:48 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class XMLView extends PlainView { // WrappedPlainView {

    public static final Color DEBUG_NORMAL_COLOR = new Color(255, 200, 200);
    public static final Color DEBUG_STOP_COLOR = new Color(0, 0, 255);
    public static final Color LINE_MARKER_COLOR = new Color(192, 192, 192);
    public static final Color ERROR_COLOR = new Color(255, 0, 0);

    private XMLScanner scanner = null;
    private XMLContext context = null;
    private XmlTextPane xmlTextPane;

    private Color bgPaintColor = null;

    /**
     * Construct a colorized view of xml text for the element. Gets the current
     * document and creates a new Scanner object.
     * 
     * @param context the styles used to colorize the view.
     * @param elem the element to create the view for.
     *
     * @param xmlTextPane
     * @throws IOException
     */
    public XMLView(XMLContext context, Element elem, XmlTextPane xmlTextPane) throws IOException {
        super( elem);

        this.context = context;
        this.xmlTextPane = xmlTextPane;
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
        paintX(g, a);
        super.paint( g, a);

        scanner.setValid( false);
    }

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
    protected int drawUnselectedText( Graphics g, int x, int y, int start, int end) throws BadLocationException {
        return XMLViewUtilities.drawUnselectedText( this, scanner, context, g, x, y, start, end);
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
    protected int drawSelectedText( Graphics g, int x, int y, int start, int end) throws BadLocationException {
        return XMLViewUtilities.drawSelectedText( this, scanner, context, g, x, y, start, end);
    }

    protected void drawLine(int lineIndex, Graphics g, int x, int y) {
        this.bgPaintColor = null;

        if (xmlTextPane.getErrorLine() == lineIndex) {
            bgPaintColor = ERROR_COLOR;
        }

        if (bgPaintColor == null && xmlTextPane.getStopDebugLine() == lineIndex) {
            bgPaintColor = DEBUG_STOP_COLOR;
        }

        if (bgPaintColor == null && xmlTextPane.getMarkerLine() == lineIndex) {
            bgPaintColor = LINE_MARKER_COLOR;
        }

        if (bgPaintColor == null) {
            BreakpointCollection breakpoints = xmlTextPane.getBreakpoints();
            if (breakpoints != null && breakpoints.isThereBreakpoint(lineIndex)) {
                bgPaintColor = DEBUG_NORMAL_COLOR;
            }
        }

        super.drawLine(lineIndex, g, x, y);
    }




    public void paintX(Graphics g, Shape a) {
        Rectangle alloc = (Rectangle) a;
        Rectangle clip = g.getClipBounds();
        int fontHeight = metrics.getHeight();
        int heightBelow = (alloc.y + alloc.height) - (clip.y + clip.height);
        int linesBelow = Math.max(0, heightBelow / fontHeight);
        int heightAbove = clip.y - alloc.y;
        int linesAbove = Math.max(0, heightAbove / fontHeight);
        int linesTotal = alloc.height / fontHeight;

        if (alloc.height % fontHeight != 0) {
            linesTotal++;
        }

        // update the visible lines
        Rectangle lineArea = lineToRect(a, linesAbove);
        int y = lineArea.y + metrics.getAscent();
        
        Element map = getElement();
    	int lineCount = map.getElementCount();
        int endLine = Math.min(lineCount, linesTotal - linesBelow);
        for (int line = linesAbove; line < endLine; line++) {
            this.bgPaintColor = null;
            
            if (xmlTextPane.getErrorLine() == line) {
                bgPaintColor = ERROR_COLOR;
            }

            if (bgPaintColor == null && xmlTextPane.getStopDebugLine() == line) {
                bgPaintColor = DEBUG_STOP_COLOR;
            }

            if (bgPaintColor == null && xmlTextPane.getMarkerLine() == line) {
                bgPaintColor = LINE_MARKER_COLOR;
            }

            if (bgPaintColor == null) {
                BreakpointCollection breakpoints = xmlTextPane.getBreakpoints();
                if (breakpoints != null && breakpoints.isThereBreakpoint(line)) {
                    bgPaintColor = DEBUG_NORMAL_COLOR;
                }
            }

            if (bgPaintColor != null) {
                g.setColor(bgPaintColor);
                final FontMetrics fontMetrics = g.getFontMetrics();
                final int rectY = y - fontMetrics.getAscent();
                g.fillRect(0, rectY, getContainer().getWidth(), fontHeight);
            }
            
            y += fontHeight;
	    }
    }


    public Color getBgPaintColor() {
        return bgPaintColor;
    }
    
}