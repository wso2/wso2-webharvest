/*
 * $Id: XMLContext.java,v 1.1 2005/03/19 12:21:47 edankert Exp $
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

import java.awt.Color;
import java.awt.Font;
import java.util.Hashtable;

import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/**
 * A list of styles used to render syntax-highlighted XML text.
 * <p>
 * <b>Note: </b> The XML Editor package is based on the JavaEditorKit example as
 * described in the article <i>'Customizing a Text Editor' </i> by <b>Timothy
 * Prinzing </b>. See:
 * http://java.sun.com/products/jfc/tsc/articles/text/editor_kit/
 * </p>
 * 
 * @version $Revision: 1.1 $, $Date: 2005/03/19 12:21:47 $
 * @author Edwin Dankert <edankert@gmail.com>
 */
public class XMLContext extends StyleContext {
    private static final long serialVersionUID = 3905522717859263025L;

    private Hashtable styles = null;

    /**
     * Constructs a set of style objects to represent XML lexical tokens and
     * initialises these tokens with a font style and color.
     */
    public XMLContext() {
        super();

        styles = new Hashtable();

        setDefaultStyles();
    }

    // Set the default styles.
    private void setDefaultStyles() {
        setStyle( XMLStyleConstants.ELEMENT_NAME, new Color( 0, 51, 102), Font.PLAIN);
        setStyle( XMLStyleConstants.ELEMENT_VALUE, Color.black, Font.PLAIN);
        setStyle( XMLStyleConstants.ELEMENT_PREFIX, new Color( 0, 102, 102), Font.PLAIN);

        setStyle( XMLStyleConstants.ATTRIBUTE_NAME, new Color( 153, 51, 51), Font.PLAIN);
        setStyle( XMLStyleConstants.ATTRIBUTE_VALUE, new Color( 102, 0, 0), Font.PLAIN);
        setStyle( XMLStyleConstants.ATTRIBUTE_PREFIX, new Color( 0, 102, 102), Font.PLAIN);

        setStyle( XMLStyleConstants.NAMESPACE_NAME, new Color( 102, 102, 102), Font.PLAIN);
        setStyle( XMLStyleConstants.NAMESPACE_VALUE, new Color( 0, 51, 51), Font.PLAIN);
        setStyle( XMLStyleConstants.NAMESPACE_PREFIX, new Color( 0, 102, 102), Font.PLAIN);

        setStyle( XMLStyleConstants.ENTITY, new Color( 102, 102, 102), Font.PLAIN);
        setStyle( XMLStyleConstants.COMMENT, new Color( 153, 153, 153), Font.PLAIN);
        setStyle( XMLStyleConstants.CDATA, new Color(0, 0, 0), Font.PLAIN);
        setStyle( XMLStyleConstants.SPECIAL, new Color( 102, 102, 102), Font.PLAIN);
    }

    /**
     * Sets the styles, like foreground color and Font style.
     * 
     * @param token the token to set the font for.
     * @param foreground the foreground color for the token.
     * @param style the font-style value for the token.
     */
    public void setStyle( String token, Color foreground, int style) {
        setForeground( token, foreground);
        setFontStyle( token, style);
    }

    /**
     * Sets the font to use for a lexical token with the given value.
     * 
     * @param token the token to set the font for.
     * @param style the font-style value for the token.
     */
    public void setFontStyle( String token, int style) {

        Style s = this.getStyle( token);

        StyleConstants.setItalic( s, (style & Font.ITALIC) > 0);
        StyleConstants.setBold( s, (style & Font.BOLD) > 0);
    }

    /**
     * Sets the foreground color to use for a lexical token with the given
     * value.
     * 
     * @param token the token to set the foreground for.
     * @param color the foreground color value for the token.
     */
    public void setForeground( String token, Color color) {

        Style s = getStyle( token);
        StyleConstants.setForeground( s, color);
    }

    /**
     * Gets the foreground color to use for a lexical token with the given
     * value.
     * 
     * @param token the style value for the token.
     * @return the foreground color value for the token.
     */
    public Color getForeground( String token) {
        if ( token != null) {
            Style s = (Style)styles.get( token);
            
            if ( s != null) {
                return super.getForeground( s);
            }
        }

        return Color.black;
    }

    /**
     * Fetch the font to use for a lexical token with the given scan value.
     * 
     * @param style the style.
     * 
     * @return the font style
     */
    public int getFontStyle( Style style) {
        int fontStyle = Font.PLAIN;

        if ( style != null) {
            if ( StyleConstants.isItalic( style)) {
                fontStyle += Font.ITALIC;
            }

            if ( StyleConstants.isBold( style)) {
                fontStyle += Font.BOLD;
            }
        }

        return fontStyle;
    }

    /**
     * Return the foreground color.
     * 
     * @param style the style
     * 
     * @return the foreground color
     */
    public Color getForeground( Style style) {
        if ( style != null) {
            return super.getForeground( style);
        }

        return null;
    }

    /**
     * Return the style for the token.
     * 
     * @param token the style identifier.
     * 
     * @return the style.
     */
    public Style getStyle( String token) {
        if ( token != null) {
            Style result = (Style)styles.get( token);
            
            if ( result == null) {
                result = new NamedStyle();
                styles.put( token, result);
            }

            return result;
        }

        return null;
    }
}
