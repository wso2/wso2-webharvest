package org.webharvest.gui;

import org.webharvest.definition.DefinitionResolver;
import org.webharvest.definition.ElementInfo;
import org.webharvest.gui.component.*;
import org.webharvest.utils.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MenuKeyEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Instance of the class is responsible for auto completion of defined tags and
 * attributes in the Web-Harvest XML configuration. It wraps instance of XML editor
 * pane and popup menu that offers context specific set of tags/attributes in the
 * editor. 
 *
 * @author: Vladimir Nikic
 * Date: May 24, 2007
 */
public class AutoCompleter {

    // editor context which decides about auto completion type  
    private static final int TAG_CONTEXT = 0;
    private static final int ATTRIBUTE_CONTEXT = 1;
    private static final int ATTRIBUTE_VALUE_CONTEXT = 2;

    // special XML constructs
    private static final String CDATA_NAME = "<![CDATA[ ... ]]>";
    private static final String XML_COMMENT_NAME = "<!-- ... -->";

    // popup window look & feel
    private static final Color BG_COLOR = new Color(235, 244, 254);

    // popup font
    private static final Font POPUP_FONT = new Font("Monospaced", Font.PLAIN, 12);

    /**
     * Class that provides listener for key events inside completer popup menu.
     */
    private class CompleterKeyListener extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            char ch = e.getKeyChar();
            int code = e.getKeyCode();
            if ( (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '-' || code == KeyEvent.VK_BACK_SPACE ) {
                Document document = xmlPane.getDocument();
                int pos = xmlPane.getCaretPosition();
                try {
                    // deleting or inserting new character
                    if (code == MenuKeyEvent.VK_BACK_SPACE) {
                        if ( pos > 0 && document.getLength() > 0 ) {
                            document.remove(pos - 1, 1);
                        }
                    } else {
                        document.insertString(pos, String.valueOf(ch), null);
                    }
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
                popupMenu.setVisible(false);
                autoComplete();
            } else if (code == KeyEvent.VK_ENTER) {
                popupMenu.setVisible(false);
                doComplete();
            }
        }
    }

    // instance of popup menu used as auto completion popup window
    private JPopupMenu popupMenu = new JPopupMenu();

    // auto-completer list model
    private DefaultListModel model = new DefaultListModel() {
        public void addElement(Object obj) {
            super.addElement(" " + obj + " ");
        }
    };

    // auto completer list
    private JList list = new JList(model);

    // xml pane instance which this auto completer is bound to
    private XmlTextPane xmlPane;

    // current context for auto cempletion
    private transient int context = TAG_CONTEXT;

    // length of prefix that user already has typed
    private int prefixLength;

    // allowed elements
    private Map elementInfos;

    /**
     * Constructor.
     * @param xmlPane
     */
    public AutoCompleter(final XmlTextPane xmlPane) {
        this.xmlPane = xmlPane;

        this.list.setBackground(BG_COLOR);
        this.list.setFont(POPUP_FONT);
        this.list.setSelectionMode(ListSelectionModel .SINGLE_SELECTION);
        this.list.addKeyListener(new CompleterKeyListener());
        this.list.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if ( e.getClickCount() > 1) {
                    popupMenu.setVisible(false);
                    doComplete();
                }
            }
        });

        this.popupMenu.setBorder( new EmptyBorder(1, 1, 1, 1) );
        this.elementInfos = DefinitionResolver.getElementInfos();
    }

    private void defineTagsMenu(String prefix) {
        if (prefix != null) {
            prefix = prefix.toLowerCase();
        }

        this.model.clear();

        Iterator iterator = this.elementInfos.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String) entry.getKey();
            if ( prefix == null || key.toLowerCase().startsWith(prefix) ) {
                ElementInfo elementInfo = (ElementInfo) entry.getValue();
                model.addElement(elementInfo.getName());
            }
        }

        boolean addCData = CDATA_NAME.toLowerCase().startsWith("<" + prefix);
        boolean addXmlComment = XML_COMMENT_NAME.toLowerCase().startsWith("<" + prefix);

        if (addCData || addXmlComment) {
            if (addCData) {
                model.addElement(CDATA_NAME);
            }
            if (addXmlComment) {
                model.addElement(XML_COMMENT_NAME);
            }
        }
    }

    private void defineAttributesMenu(String elementName, String prefix) {
        elementName = elementName.toLowerCase();
        prefix = prefix.toLowerCase();

        this.model.clear();

        ElementInfo elementInfo = DefinitionResolver.getElementInfo(elementName);
        if (elementInfo != null) {
            for (Object attObj: elementInfo.getAttsSet()) {
                if (attObj != null) {
                    String att = ((String)attObj).toLowerCase();
                    if ( att.startsWith(prefix) && !"id".equals(att) ) {
                        model.addElement(att);
                    }
                }
            }
        }
    }

    private void defineAttributeValuesMenu(String tagName, String attributeName, String attValuePrefix) {
        this.model.clear();

        ElementInfo elementInfo = DefinitionResolver.getElementInfo(tagName);
        if (elementInfo != null) {
            String[] suggs = elementInfo.getAttributeValueSuggestions(attributeName);
            if (suggs != null) {
                for (String s: suggs) {
                    if (s.toLowerCase().startsWith(attValuePrefix)) {
                        model.addElement(s);
                    }
                }
            }
        }
    }

    /**
     * Performs auto completion.
     */
    public void autoComplete() {
        try {
            Document document = this.xmlPane.getDocument();
            int offset = this.xmlPane.getCaretPosition();
            String text = document.getText(0, offset);

            int openindex = text.lastIndexOf('<');
            int closeindex = text.lastIndexOf('>');

            if (openindex > closeindex) {                   // inside tag definition
                text = text.substring(openindex);

                String tagName = text.length() > 1 ? getIdentifierAtStart(text.substring(1)) : null;
                String trimmedText = text.trim();

                this.context = TAG_CONTEXT;

                if (tagName != null && tagName.length() > 0) {
                    int quoteIndex = Math.max( trimmedText.lastIndexOf("\""), trimmedText.lastIndexOf("\'") );
                    if (quoteIndex > 0) {
                        int eqIndex = trimmedText.lastIndexOf("=");
                        if ( eqIndex >= 0 && eqIndex < quoteIndex && "".equals(trimmedText.substring(eqIndex + 1, quoteIndex).trim()) ) {
                            int firstQuoteIndex = trimmedText.indexOf("\"", eqIndex);
                            if (firstQuoteIndex < 0) {
                                firstQuoteIndex = trimmedText.indexOf("\'", eqIndex);
                            }
                            if (firstQuoteIndex < 0 || firstQuoteIndex == quoteIndex) {
                                String attValuePrefix = trimmedText.substring(quoteIndex + 1);
                                trimmedText = trimmedText.substring(0, eqIndex).trim();
                                String attName = getIdentifierFromEnd(trimmedText);
                                if (attName != null && attName.length() > 0) {
                                    this.context = ATTRIBUTE_VALUE_CONTEXT;
                                    defineAttributeValuesMenu( tagName.toLowerCase().trim(), attName.toLowerCase().trim(), attValuePrefix.toLowerCase().trim() );
                                }
                            }
                        }
                    }
                }

                if (this.context != ATTRIBUTE_VALUE_CONTEXT) {
                    String identifier = getIdentifierFromEnd(text);
                    if ( containWhitespaces(text) ) {           // attributes context
                        this.context = ATTRIBUTE_CONTEXT;
                        String elementName = getIdentifierFromStart(text);
                        defineAttributesMenu(elementName, identifier);
                        this.prefixLength = identifier.length();
                    } else {
                        this.context = TAG_CONTEXT;         // tag name context
                        defineTagsMenu(identifier);
                        this.prefixLength = identifier.length() + 1;
                    }
                }
            } else {                                        // ouside tag definition
                this.context = TAG_CONTEXT;
                defineTagsMenu("");
                this.prefixLength = 0;
            }

            Rectangle position = this.xmlPane.modelToView(offset);
            if (this.model.getSize() > 0) {
                this.popupMenu.removeAll();

                this.list.setVisibleRowCount(Math.min(12, model.getSize()));
                JScrollPane scrollPane = new WHScrollPane(list);

                this.popupMenu.add(scrollPane);
                this.popupMenu.show( this.xmlPane, (int)position.getX(), (int)(position.getY() + position.getHeight()) );
                this.list.grabFocus();
                this.list.setSelectedIndex(0);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param text
     * @return True if specified string contains any whitespace characters, false otherwise.
     */
    private boolean containWhitespaces(String text) {
        int len = text.length();
        for (int i = 0; i < len; i++) {
            if ( Character.isWhitespace(text.charAt(i)) ) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param text
     * @return Maximal peace at the start of specified string which is valid tag or attribute name. 
     */
    private String getIdentifierFromStart(String text) {
        if ( text.startsWith("<") ) {
            text = text.substring(1);
        }
        
        StringBuffer result = new StringBuffer();
        int len = text.length();
        for (int i = 0; i < len; i++) {
            char ch = text.charAt(i);
            if ( Character.isLetter(ch) || ch == '-' || ch == '_' || ch == '!' ) {
                result.append(ch);
            } else {
                break;
            }
        }

        return result.toString(); 
    }

    /**
     * @param text
     * @return Maximal peace at the end of specified string which is valid tag or attribute name.
     */
    private String getIdentifierFromEnd(String text) {
        StringBuffer result = new StringBuffer();
        for (int i = text.length() - 1; i >= 0; i--) {
            char ch = text.charAt(i);
            if ( Character.isLetter(ch) || ch == '-' || ch == '_' || ch == '!' ) {
                result.insert(0, ch);
            } else {
                break;
            }
        }

        return result.toString();
    }

    /**
     * @param text
     * @return Identifier name at start of given string.
     */
    private String getIdentifierAtStart(String text) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if ( Character.isLetter(ch) || (i != 0 && (ch == '-' || ch == '_' || ch == '!'))  ) {
                result.append(ch);
            } else {
                break;
            }
        }
        return result.toString();
    }

    /**
     * Action for auto complete items
     */
    public void doComplete() {
        String selectedValue = (String) list.getSelectedValue();
        if (selectedValue != null) {
            selectedValue = selectedValue.trim();
            try {
                if (this.context == TAG_CONTEXT) {
                    completeTag(selectedValue);
                } else if (this.context == ATTRIBUTE_VALUE_CONTEXT) {
                    completeAttributeValue(selectedValue);
                } else {
                    completeAttribute(selectedValue);
                }
            } catch(BadLocationException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void completeAttribute(String name) throws BadLocationException {
        Document document = xmlPane.getDocument();
        int pos = xmlPane.getCaretPosition();
        String cursorChar = document.getText(pos, 1);
        boolean toAppendSpace = !">".equals(cursorChar) && !"".equals(cursorChar.trim()) && !"/".equals(cursorChar);
        String template = (name + "=\"\"" + (toAppendSpace ? " " : "")).substring(this.prefixLength);

        document.insertString(pos, template, null);
        xmlPane.setCaretPosition( xmlPane.getCaretPosition() - 1 );
    }

    private void completeAttributeValue(String value) throws BadLocationException {
        Document document = xmlPane.getDocument();
        int pos = xmlPane.getCaretPosition();
        String text = document.getText(0, pos);
        int startTagIndex = text.lastIndexOf("<");
        if (startTagIndex >= 0) {
            int quoteIndex = Math.max( text.lastIndexOf("\""), text.lastIndexOf("\'") );
            if (quoteIndex > 0 && quoteIndex > startTagIndex) {
                document.remove(quoteIndex + 1, pos - quoteIndex - 1);
                document.insertString(quoteIndex + 1, value, null);
            }
        }
    }

    private void completeTag(String name) throws BadLocationException {
        Document document = xmlPane.getDocument();
        int pos = xmlPane.getCaretPosition();

        if ( CDATA_NAME.equals(name) ) {
            document.insertString(pos, "<![CDATA[  ]]>".substring(this.prefixLength), null);
            xmlPane.setCaretPosition( xmlPane.getCaretPosition() - 4 );
        } else if ( XML_COMMENT_NAME.equals(name) ) {
            document.insertString(pos, "<!--  -->".substring(this.prefixLength), null);
            xmlPane.setCaretPosition( xmlPane.getCaretPosition() - 4 );
        } else {
            ElementInfo info = DefinitionResolver.getElementInfo(name);
            if (info != null) {
                String template = info.getTemplate(true).substring(this.prefixLength);
                document.insertString(pos, template, null);
                int closingIndex = template.lastIndexOf("</");
                if (closingIndex >= 0) {
                    xmlPane.setCaretPosition( xmlPane.getCaretPosition() - template.length() + closingIndex );
                }
            }
        }
    }

}