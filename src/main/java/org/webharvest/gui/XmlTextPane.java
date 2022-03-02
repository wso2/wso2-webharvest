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
package org.webharvest.gui;

/**
 * @author: Vladimir Nikic
 * Date: Apr 20, 2007
 */

import org.bounce.text.xml.XMLEditorKit;
import org.bounce.text.xml.XMLStyleConstants;
import org.webharvest.utils.CommonUtil;

import javax.swing.*;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;

public class XmlTextPane extends JEditorPane {

    /**
     * Action which occures on Ctrl-D key pressed inside the editor.
     * It implements duplicating od current line if there is no selection, or aezaa
     * duplication of selected text if selection is not empty.
     */
    private class DuplicateAction {
        public void execute() {
            if (!isEditable() || !isEnabled()) {
                return;
            }

            try {
                int position = lastClickPoint != null ? viewToModel(lastClickPoint) : getCaretPosition();
                lastClickPoint = null;
                Document document = getDocument();
                String selectedText = getSelectedText();
                if ( selectedText != null && !CommonUtil.isEmpty(selectedText) ) {
                    final int selectionEnd = getSelectionEnd();
                    document.insertString(selectionEnd, selectedText, null);
                    select(selectionEnd, selectionEnd + selectedText.length());
                } else {
                    final int docLen = document.getLength();
                    int fromIndex = Math.max( 0, getText(0, position).lastIndexOf('\n') );
                    int toIndex = getText(fromIndex + 1, docLen - fromIndex).indexOf('\n');
                    toIndex = toIndex < 0 ? docLen : fromIndex + toIndex;
                    String textToDuplicate = getText(fromIndex, toIndex - fromIndex + 1);
                    if (!textToDuplicate.startsWith("\n")) {
                        textToDuplicate = "\n" + textToDuplicate;
                    }
                    if (textToDuplicate.endsWith("\n")) {
                        textToDuplicate = textToDuplicate.substring(0, textToDuplicate.length() - 1);
                    }
                    document.insertString(Math.min(docLen, toIndex + 1), textToDuplicate, null);
                    setCaretPosition(position + textToDuplicate.length());
                }
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * Action which occures on Ctrl-/ key pressed inside the editor.
     * It implements commenting/uncommenting current line, or selected
     * text, depending on whether selection exists or not.
     */
    private class CommentAction {
        public void execute() {
            if (!isEditable() || !isEnabled()) {
                return;
            }
            int position = lastClickPoint != null ? viewToModel(lastClickPoint) : getCaretPosition();
            lastClickPoint = null;
            Document document = getDocument();
            String selectedText = getSelectedText();

            try {
                if ( selectedText != null && !CommonUtil.isEmpty(selectedText) ) {
                    String trimmed = selectedText.trim();
                    if ( trimmed.startsWith("<!--") && trimmed.endsWith("-->") ) {
                        StringBuffer buffer = new StringBuffer(selectedText);
                        int pos = buffer.indexOf("<!--");
                        buffer.delete(pos, pos + 4);
                        pos = buffer.lastIndexOf("-->");                           
                        buffer.delete(pos, pos + 3);
                        replaceSelection( buffer.toString() );
                    } else {
                        String newSelection = "<!--" + selectedText + "-->";
                        replaceSelection(newSelection);
                    }
                } else {
                    final int docLen = document.getLength();
                    int fromIndex = Math.max( 0, getText(0, position).lastIndexOf('\n') );
                    int toIndex = getText(fromIndex + 1, docLen - position).indexOf('\n');
                    toIndex = toIndex < 0 ? docLen : fromIndex + toIndex;
                    String textToComment = getText(fromIndex, toIndex - fromIndex + 1);

                    if (textToComment.startsWith("\n")) {
                        textToComment = textToComment.substring(1);
                        fromIndex++;
                    }
                    if (textToComment.endsWith("\n")) {
                        textToComment = textToComment.substring(0, textToComment.length() - 1);
                        toIndex--;
                    }
                    String trimmed = textToComment.trim();
                    if ( trimmed.startsWith("<!--") && trimmed.endsWith("-->") ) {
                        int pos = textToComment.lastIndexOf("-->");
                        document.remove(fromIndex + pos, 3);
                        pos = textToComment.indexOf("<!--");
                        document.remove(fromIndex + pos, 4);
                    } else {
                        document.insertString(Math.min(toIndex + 1, docLen), "-->", null);
                        document.insertString(fromIndex, "<!--", null);
                    }
                }
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * Action which occures on Ctrl-Y key pressed inside the editor.
     * It implements deleting current line.
     */
    private class DeleteLineAction extends TextAction {
        public DeleteLineAction() {
            super("delete-line");
        }

        public void actionPerformed(ActionEvent e) {
            JTextComponent textComponent = getTextComponent(e);
            if ( !textComponent.isEditable() || !textComponent.isEnabled() ) {
                return;
            }

            try {
                final int position = getCaretPosition();
                final Document document = getDocument();
                int docLen = document.getLength();

                if (docLen == 0) {
                    return;
                }

                int fromIndex = Math.max( 0, getText(0, position).lastIndexOf('\n') );
                int toIndex = getText(fromIndex + 1, docLen - fromIndex - 1).indexOf('\n');
                toIndex = toIndex < 0 ? docLen : fromIndex + toIndex + 1;
                String text = getText(fromIndex, toIndex - fromIndex);
                if ( text.startsWith("\n") || toIndex >= docLen) {
                    document.remove(fromIndex, toIndex - fromIndex);
                } else {
                    document.remove(fromIndex, toIndex - fromIndex + 1);
                }

                int newPosition = 0;
                if (fromIndex > 0) {
                    newPosition = fromIndex + 1;
                }
                docLen = document.getLength();
                if (newPosition > docLen) {
                    newPosition = getText().lastIndexOf('\n') + 1;
                }
                setCaretPosition(newPosition);
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * Action which occures on TAB key pressed inside the editor. It implements clever
     * block indenting - when selection contains more than one line of text, the whole
     * block is indented one tab place to the right.
     */
    private class TabAction extends TextAction {
        public TabAction() {
            super(DefaultEditorKit.insertTabAction);
        }

        private void indentText(JTextComponent textComponent) throws BadLocationException {
            String selectedText = textComponent.getSelectedText();
            int newLineIndex = selectedText != null ? selectedText.indexOf('\n') : -1;


            if (newLineIndex >= 0) {
                int originalSelectionStart = textComponent.getSelectionStart();
                int selectionStart = originalSelectionStart;
                int selectionEnd = textComponent.getSelectionEnd();

                int lastNewLineBeforeSelection = textComponent.getText(0, selectionStart).lastIndexOf('\n');
                int begin = lastNewLineBeforeSelection >= 0 ? lastNewLineBeforeSelection : 0;
                int end = selectionEnd;

                String text = textComponent.getText(begin, end - begin);
                int len = text.length();
                StringBuffer out = new StringBuffer(len);
                if (lastNewLineBeforeSelection < 0) {
                    out.insert(0, '\t');
                    selectionStart++;
                    selectionEnd++;
                }
                for (int i = 0; i < len; i++) {
                    char ch = text.charAt(i);
                    out.append(ch);
                    if (ch == '\n' && i < len - 1) {
                        out.append("\t");
                        selectionEnd++;
                        if (begin + i < originalSelectionStart) {
                            selectionStart++;
                        }
                    }
                }

                textComponent.select(begin, end);
                textComponent.replaceSelection(out.toString());

                textComponent.select(selectionStart, selectionEnd);
            } else {
                textComponent.replaceSelection("\t");
            }
        }

        public void actionPerformed(ActionEvent e) {
            JTextComponent textComponent = getTextComponent(e);
            if ( !textComponent.isEditable() || !textComponent.isEnabled() ) {
                return;
            }

            try {
                indentText(textComponent);
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * Action which occures on SHIFT-TAB key pressed inside the editor. It implements clever
     * block outdenting - when selection contains more than one line of text, the whole
     * block is outdented one tab place to the left. 
     */
    private class ShiftTabAction extends TextAction {
        public ShiftTabAction() {
            super("shift-tab-action");
        }

        private void outdentText(JTextComponent textComponent) throws BadLocationException {
            int tabSize = ((Integer)textComponent.getDocument().getProperty(PlainDocument.tabSizeAttribute)).intValue();

            String selectedText = textComponent.getSelectedText();
            int newLineIndex = selectedText != null ? selectedText.indexOf('\n') : -1;

            if (newLineIndex >= 0) {
                int originalSelectionStart = textComponent.getSelectionStart();
                int selectionStart = originalSelectionStart;
                int selectionEnd = textComponent.getSelectionEnd();

                int lastNewLineBeforeSelection = textComponent.getText(0, selectionStart).lastIndexOf('\n');
                int begin = lastNewLineBeforeSelection >= 0 ? lastNewLineBeforeSelection : 0;
                int end = selectionEnd;

                String text = textComponent.getText(begin, end - begin);
                if (lastNewLineBeforeSelection < 0) {
                    text = "\n" + text;
                }
                int len = text.length();
                StringBuffer out = new StringBuffer(len);
                for (int i = 0; i < len; i++) {
                    char ch = text.charAt(i);
                    out.append(ch);
                    if (ch == '\n' && i < len - 1) {
                        char next = text.charAt(i + 1);
                        int stripCount = 0;
                        if (next == '\t') {
                            stripCount = 1;
                        } else {
                            for ( ; stripCount < tabSize && i + 1 + stripCount < len; stripCount++ ) {
                                next = text.charAt(i + 1 + stripCount);
                                if ( next != ' ' && next != '\t' ) {
                                    break;
                                }
                            }
                        }

                        selectionEnd -= stripCount;
                        if (i + begin < originalSelectionStart - 1) {
                            selectionStart -= stripCount;
                        }
                        i += stripCount;
                    }
                }

                textComponent.select(begin, end);
                textComponent.replaceSelection(lastNewLineBeforeSelection < 0 ? out.toString().substring(1) : out.toString());

                textComponent.select(selectionStart, selectionEnd);
            }
        }

        public void actionPerformed(ActionEvent e){
            JTextComponent textComponent = getTextComponent(e);
            if ( !textComponent.isEditable() || !textComponent.isEnabled() ) {
                return;
            }

            try {
                outdentText(textComponent);
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
        }
    }

    private UndoManager undoManager = new UndoManager();

    private DeleteLineAction deleteLineAction = new DeleteLineAction();
    private TabAction tabAction = new TabAction();
    private ShiftTabAction shiftTabAction = new ShiftTabAction();
    private DuplicateAction duplicateAction = new DuplicateAction();
    private CommentAction commentAction = new CommentAction();

    private BreakpointCollection breakpoints = new BreakpointCollection();
    private int errorLine = -1;
    private int markerLine = -1;
    private int stopDebugLine = -1;

    private Point lastClickPoint;

    public XmlTextPane() {
        XMLEditorKit kit = new XMLEditorKit(true, this);

        kit.setLineWrappingEnabled(false);

        kit.setStyle( XMLStyleConstants.ELEMENT_NAME, new Color(128, 0, 0), Font.PLAIN);
        kit.setStyle( XMLStyleConstants.ELEMENT_VALUE, new Color(0, 0, 0), Font.PLAIN);
        kit.setStyle( XMLStyleConstants.ELEMENT_PREFIX, new Color(128, 0, 0), Font.PLAIN);

        kit.setStyle( XMLStyleConstants.ATTRIBUTE_NAME, new Color(255, 0, 0), Font.PLAIN);
        kit.setStyle( XMLStyleConstants.ATTRIBUTE_VALUE, new Color(0, 0, 0), Font.PLAIN);
        kit.setStyle( XMLStyleConstants.ATTRIBUTE_PREFIX, new Color(128, 0, 0), Font.PLAIN);

        kit.setStyle( XMLStyleConstants.NAMESPACE_NAME, new Color(102, 102, 102), Font.PLAIN);
        kit.setStyle( XMLStyleConstants.NAMESPACE_VALUE, new Color(0, 51, 51), Font.PLAIN);
        kit.setStyle( XMLStyleConstants.NAMESPACE_PREFIX, new Color(102, 102, 102), Font.PLAIN);

        kit.setStyle( XMLStyleConstants.ENTITY, new Color(0, 0, 0), Font.PLAIN);
        kit.setStyle( XMLStyleConstants.COMMENT, new Color(153, 153, 153), Font.PLAIN);
        kit.setStyle( XMLStyleConstants.CDATA, new Color(0, 0, 0), Font.PLAIN);
        kit.setStyle( XMLStyleConstants.SPECIAL, new Color(0, 0, 0), Font.PLAIN);

        this.setEditorKit(kit);

        this.setFont( new Font( "Monospaced", Font.PLAIN, 12));

        this.registerKeyboardAction(shiftTabAction, KeyStroke.getKeyStroke(KeyEvent.VK_TAB, ActionEvent.SHIFT_MASK), JComponent.WHEN_FOCUSED);
        this.registerKeyboardAction(deleteLineAction, KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
        ActionListener escAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearMarkerLine();
                clearErrorLine();
            }
        };
        this.registerKeyboardAction(escAction, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_FOCUSED);
        this.getActionMap().put(tabAction.getValue(Action.NAME), tabAction);
    }

    public boolean getScrollableTracksViewportWidth() {
        //should not allow text to be wrapped
      return false;
    }

    public void undo() {
        if ( this.undoManager.canUndo() ) {
            this.undoManager.undo();
        }
    }

    public void redo() {
        if ( this.undoManager.canRedo() ) {
            this.undoManager.redo();
        }
    }

    public UndoableEditListener getUndoManager() {
        return undoManager;
    }

    /**
     * @return True if some text is selected, false otherwise
     */
    public boolean hasSelection() {
        String selectedText = this.getSelectedText();
        return selectedText != null && !"".equals(selectedText);
    }

    public void duplicate() {
        duplicateAction.execute();
    }

    public void comment() {
        commentAction.execute();
    }

    public void toggleBreakpoint() {
        if (isEnabled()) {
            try {
                int position = lastClickPoint != null ? viewToModel(lastClickPoint) : getCaretPosition();
                lastClickPoint = null;
                if (position >= 0) {
                    String textToCaret = getDocument().getText(0, position);
                    int lineCount = 0;
                    for (int i = 0; i < textToCaret.length(); i++) {
                        if (textToCaret.charAt(i) == '\n') {
                            lineCount++;
                        }
                    }
                   if (breakpoints.isThereBreakpoint(lineCount)) {
                       breakpoints.removeBreakpoint(lineCount);
                   } else {
                       breakpoints.addBreakpoint(new BreakpointInfo(lineCount));
                   }
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }

            Component parent = GuiUtils.getParentOfType(this, XmlEditorScrollPane.class);
            if (parent != null) {
                ((XmlEditorScrollPane)parent).onDocChanged();
            }
            repaint();
        }
    }

    private int getPositionForLine(int line) {
        String text = getText();
        int lineCount = 0;
        int pos = 0;
        for (; pos < text.length(); pos++) {
            if (lineCount == line) {
                break;
            }
            if (text.charAt(pos) == '\n') {
                lineCount++;
            }
        }

        return pos;
    }

    private void scrollToLine(final int line) {
        final int posForLine = getPositionForLine(line);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    scrollRectToVisible(modelToView(posForLine));
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public BreakpointCollection getBreakpoints() {
        return breakpoints;
    }

    public int getMarkerLine() {
        return markerLine;
    }

    public void setMarkerLine(int markerLine) {
        this.markerLine = markerLine;
        repaint();
        scrollToLine(markerLine);
    }

    public void clearMarkerLine() {
        this.markerLine = -1;
        repaint();
    }

    public int getErrorLine() {
        return errorLine;
    }

    public void setErrorLine(int errorLine) {
        this.errorLine = errorLine;
        repaint();
        scrollToLine(errorLine);
    }

    public void clearErrorLine() {
        this.errorLine = -1;
        repaint();
    }

    public int getStopDebugLine() {
        return stopDebugLine;
    }

    public void setStopDebugLine(int stopDebugLine) {
        this.stopDebugLine = stopDebugLine;
        repaint();
        scrollToLine(stopDebugLine);
    }

    public void clearStopDebugLine() {
        this.stopDebugLine = -1;
        repaint();
    }

    public void setLastClickPoint(Point lastClickPoint) {
        this.lastClickPoint = lastClickPoint;
    }

    public Point getLastClickPoint() {
        return lastClickPoint;
    }
    
}