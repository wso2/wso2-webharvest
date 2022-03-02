/*
 * $Id: XMLInputStream.java,v 1.1 2005/03/19 12:21:47 edankert Exp $
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
import java.io.InputStream;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Segment;

/**
 * A XML input stream, for a XML Document.
 * 
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
class XMLInputStream extends InputStream {

    private Segment segment = null;

    private Document document = null;

    private int end = 0; // end position

    private int pos = 0; // pos in document

    private int index = 0; // index into array of the segment

    /**
     * Constructs a stream for the document.
     * 
     * @param doc
     *            the document with Xml Information.
     */
    public XMLInputStream( Document doc) {
        this.segment = new Segment();
        this.document = doc;

        end = document.getLength();
        pos = 0;

        try {
            loadSegment();
        } catch ( IOException ioe) {
            throw new Error( "unexpected: " + ioe);
        }
    }

    /**
     * Sets the new range to be scanned for the stream and loads the necessary
     * information.
     * 
     * @param start
     *            the start of the segment.
     * @param end
     *            the end of the segment.
     */
    public void setRange( int start, int end) {
        this.end = end;
        pos = start;

        try {
            loadSegment();
        } catch ( IOException ioe) {
            throw new Error( "unexpected: " + ioe);
        }
    }

    /**
     * Reads the next byte of data from this input stream. The value byte is
     * returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the
     * stream has been reached, the value <code>-1</code> is returned.
     * 
     * @return the next byte of data, or <code>-1</code> if the end of the
     *         stream is reached.
     *         
     * @throws IOException
     */
    public int read() throws IOException {
        if ( index >= segment.offset + segment.count) {
            if ( pos >= end) {
                // no more data
                return -1;
            }

            loadSegment();
        }

        return segment.array[index++];
    }

    // Loads the segment with new information if necessary...
    private void loadSegment() throws IOException {
        try {
            int n = Math.min( 1024, end - pos);

            document.getText( pos, n, segment);
            pos += n;

            index = segment.offset;
        } catch ( BadLocationException e) {
            throw new IOException( "Bad location");
        }
    }
}
