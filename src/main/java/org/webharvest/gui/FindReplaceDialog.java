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

import org.webharvest.gui.component.CommonDialog;
import org.webharvest.gui.component.WHComboBox;
import org.webharvest.gui.component.WHCheckBox;
import org.webharvest.gui.component.WHRadioButton;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class FindReplaceDialog extends CommonDialog {

    private static final int OPERATION_FIND = 1;
    private static final int OPERATION_REPLACE = 2; 

    private int operation = OPERATION_FIND;

    // Ide instance where this dialog belongs.
    private Frame parentFrame;

    // settiongs fields
    private JComboBox searchComboBox;
    private JLabel replaceLabel;
    private JComboBox replaceComboBox;
    private JTextComponent textComponent;

    private JCheckBox caseSensitiveCheckBox;
    private JCheckBox regularExpressionsCheckBox;
    private JRadioButton forwardRadioButton;
    private JRadioButton backwordRadioButton;
    private JRadioButton entireScopeRadioButton;

    public FindReplaceDialog(Frame parentFrame) throws HeadlessException {
        super("Find Text");
        this.parentFrame = parentFrame;

        addWindowListener( new WindowAdapter() {
           public void windowActivated( WindowEvent e ){
                searchComboBox.requestFocus();
                searchComboBox.getEditor().selectAll();
             }
        } );

        createGui();
    }

    private void createGui() {
        Container contentPane = this.getContentPane();

        JPanel topPanel = new JPanel(new GridBagLayout());

        contentPane.setLayout( new BorderLayout() );

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(2, 5, 2, 5);

        searchComboBox = new WHComboBox(new Object[] {}) {
            public Dimension getPreferredSize() {
                return new Dimension(300, 20);
            }
        };
        searchComboBox.setEditable(true);
        searchComboBox.getEditor().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchComboBox.setSelectedItem(e.getActionCommand());
            }
        });
        
        replaceLabel = new JLabel("Replace with: ");
        replaceComboBox = new WHComboBox(new Object[] {}) {
            public Dimension getPreferredSize() {
                return new Dimension(300, 20);
            }
        };
        replaceComboBox.setEditable(true);
        replaceComboBox.getEditor().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                replaceComboBox.setSelectedItem(e.getActionCommand());
            }
        });

        caseSensitiveCheckBox = new WHCheckBox("Case sensitive", false);

        constraints.gridx = 0;
        constraints.gridy = 0;
        topPanel.add( new JLabel("Text to find: "), constraints );

        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1;
        topPanel.add(searchComboBox, constraints );

        constraints.gridx = 0;
        constraints.gridy = 1;
        topPanel.add( replaceLabel, constraints );

        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.weightx = 1;
        topPanel.add(replaceComboBox, constraints );

        JPanel optionsPanel = createOptionsPanel();
        JPanel directionsPanel = createDirectionsPanel();

        JPanel middlePanel = new JPanel(new GridLayout(1, 2));
        middlePanel.add(optionsPanel);
        middlePanel.add(directionsPanel);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(new Insets(4, 4, 4, 4)));
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(middlePanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel( new FlowLayout(FlowLayout.CENTER) );
        
        JButton okButton = createOkButton();
        okButton.setText("Find");
        buttonPanel.add(okButton);
        buttonPanel.add(createCancelButton());

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        contentPane.add(mainPanel, BorderLayout.CENTER);

        this.pack();
    }

    private JPanel createDirectionsPanel() {
        JPanel directionsPanel = new JPanel(new GridLayout(3, 1));
        directionsPanel.setBorder( new TitledBorder("Direction") );

        ButtonGroup group = new ButtonGroup();

        this.forwardRadioButton = new WHRadioButton("Forward");
        this.forwardRadioButton.setSelected(true);
        this.backwordRadioButton = new WHRadioButton("Backword");
        this.entireScopeRadioButton = new WHRadioButton("Entire scope");

        group.add(this.forwardRadioButton);
        group.add(this.backwordRadioButton);
        group.add(this.entireScopeRadioButton);

        directionsPanel.add(this.forwardRadioButton);
        directionsPanel.add(this.backwordRadioButton);
        directionsPanel.add(this.entireScopeRadioButton);

        return directionsPanel;
    }

    private JPanel createOptionsPanel() {
        JPanel optionsPanel = new JPanel(new GridLayout(3, 1));
        optionsPanel.setBorder(new TitledBorder("Options"));
        this.caseSensitiveCheckBox = new WHCheckBox("Case sensitive");
        optionsPanel.add(this.caseSensitiveCheckBox);
        this.regularExpressionsCheckBox = new WHCheckBox("Regular expressions");
        optionsPanel.add(this.regularExpressionsCheckBox);
        return optionsPanel;
    }

    protected JRootPane createRootPane() {
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        JRootPane rootPane = new JRootPane();
        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                setVisible(false);
            }
        };
        rootPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

        stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (operation == OPERATION_FIND) {
                    find(false);
                } else {
                    replace(false);
                }
            }
        };
        rootPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);


        return rootPane;
    }

    public void open(JTextComponent textComponent, boolean isReplace) {
        setTextComponent(textComponent);
        this.operation = isReplace ? OPERATION_REPLACE : OPERATION_FIND;
        this.replaceLabel.setVisible(isReplace);
        this.replaceComboBox.setVisible(isReplace);
        this.setTitle(isReplace ? "Replace Text" : "Find Text");
        this.pack();

        setLocationRelativeTo(parentFrame);
        setVisible(true);
    }

    public boolean find(boolean backward) {
        return find(backward, true);
    }

    public boolean find(boolean backward, boolean showMessageIfNotFound) {
        String searchText = this.searchComboBox.getEditor().getItem().toString();
        if (searchText == null) {
            return false;
        }

        // there should be something to perform search on and something to search
        if ( this.textComponent == null || "".equals(searchText) ) {
            return false;
        }

        if (this.isVisible()) {
            this.searchComboBox.removeItem(searchText);
            this.searchComboBox.insertItemAt(searchText, 0);
            this.searchComboBox.setSelectedItem(searchText);
        }

        // if find/replace dialog is opened only radio button is relevant
        if ( this.isVisible() ) {
            backward = this.backwordRadioButton.isSelected(); 
        }

        int startPosition = this.textComponent.getCaretPosition();
        Document doc = this.textComponent.getDocument();
        int len = doc.getLength();

        String content = "";
        try {
            if (backward) {
                content = doc.getText(0, startPosition > 1 ? startPosition - 1 : 0);
                startPosition = 0;
            } else if ( this.isVisible() && this.entireScopeRadioButton.isSelected() ) {
                content = doc.getText(0, len);
                startPosition = 0;
            } else {
                content = doc.getText(startPosition, len - startPosition);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
            return false;
        }

        // hide this find/replace dialog
        this.setVisible(false);

        boolean isRegexp = this.regularExpressionsCheckBox.isSelected();
        boolean isCaseSensitive = this.caseSensitiveCheckBox.isSelected();
        
        if ( !isCaseSensitive && !isRegexp) {
            content = content.toLowerCase();
            searchText = searchText.toLowerCase();
        }

        // search for occurence
        int index = -1;
        int foundLen = 0;
        if (isRegexp) {
            int flags = Pattern.DOTALL;
            if (!isCaseSensitive) {
                flags |= Pattern.CASE_INSENSITIVE;
            }
            Pattern pattern = Pattern.compile(searchText, flags);
            Matcher matcher = pattern.matcher(content);

            if (!backward) {
                if (matcher.find()) {
                    index = matcher.start();
                    foundLen = matcher.end() - index;
                }
            } else {
                while (matcher.find()) {
                    index = matcher.start();
                    foundLen = matcher.end() - index;
                }
            }
        } else {
            index = backward ? content.lastIndexOf(searchText) : content.indexOf(searchText);
            foundLen = searchText.length();
        }

        if (index >= 0) {
            textComponent.requestFocusInWindow();
            textComponent.select(startPosition + index, startPosition + index + foundLen);
            
            return true;
        } else if (showMessageIfNotFound) {
            Component top = findTopComponent();
            JOptionPane.showMessageDialog(top, "Next occurrence of \"" + searchText + "\" not found.", "Information", JOptionPane.INFORMATION_MESSAGE);
        }

        return false;
    }

    /**
     * Finds the top component in the hierarchy.
     */
    private Component findTopComponent() {
        Component top = textComponent;
        while (top.getParent() != null) {
            top = top.getParent();
        }
        return top;
    }

    public void replace(boolean backward) {
        final String searchText = (String)this.searchComboBox.getSelectedItem();
        final String replaceText = (String)this.replaceComboBox.getSelectedItem();

        if (this.isVisible()) {
            if ( !"".equals(searchText) ) {
                this.searchComboBox.removeItem(searchText);
                this.searchComboBox.insertItemAt(searchText, 0);
                this.searchComboBox.setSelectedItem(searchText);
            }
            this.replaceComboBox.removeItem(replaceText);
            this.replaceComboBox.insertItemAt(replaceText, 0);
            this.replaceComboBox.setSelectedItem(replaceText);
        }

        // there should be something to perform search on and something to search
        if ( this.textComponent == null || "".equals(searchText) ) {
            this.setVisible(false);
            return;
        }

        final Object[] options = {"Replace", "Skip", "All", "Cancel"};
        final JOptionPane optionPane = new JOptionPane("Do you want to replace this occurence?",
                                                 JOptionPane.QUESTION_MESSAGE,
                                                 JOptionPane.YES_NO_CANCEL_OPTION,
                                                 null,
                                                 options,
                                                 options[0] );

        final Frame topFrame = (Frame) findTopComponent();
        final JDialog dialog = new JDialog(topFrame, "Replace", true) {
            protected JRootPane createRootPane() {
                KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
                JRootPane rootPane = new JRootPane();
                ActionListener actionListener = new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        optionPane.setValue("Cancel");
                        setVisible(false);
                    }
                };
                rootPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
                return rootPane;
            }
        };
        dialog.setContentPane(optionPane);
        optionPane.addPropertyChangeListener(
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent e) {
                    String prop = e.getPropertyName();
                    if ( dialog.isVisible() && (e.getSource() == optionPane) && (prop.equals(JOptionPane.VALUE_PROPERTY)) ) {
                        dialog.setVisible(false);
                    }
                }
            }
        );

        dialog.pack();

        Object result = null;
        boolean found;
        do {
            found = find(false);
            if (found) {
                optionPane.setValue(null);
                dialog.setVisible(true);
                result = optionPane.getValue();
                if ("Replace".equals(result)) {
                    this.textComponent.replaceSelection(replaceText);
                } else if ("All".equals(result)) {
                    int count = 0;
                    do {
                        this.textComponent.replaceSelection(replaceText);
                        count++;
                        found = find(false, false);
                    } while (found);
                    JOptionPane.showMessageDialog(topFrame, count + " occurence(s) replaced.", "Information", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } while ( found && !"All".equals(result) && !"Cancel".equals(result) );
    }

    public String getSearchText() {
        return (String) this.searchComboBox.getSelectedItem();
    }

    public void setTextComponent(JTextComponent textComponent) {
        this.textComponent = textComponent;
    }

    public void findNext(JTextComponent textComponent) {
        setTextComponent(textComponent);
        find(false);
    }

    public void findPrev(JTextComponent textComponent) {
        this.textComponent = textComponent;
        find(true);
    }

    public static void main(String[] args) {
        String s = "1ABCDEEEEHIEUIRFEEewgfuiewhf";
        Pattern pattern = Pattern.compile("\\d", Pattern.DOTALL|Pattern.UNICODE_CASE);
        Matcher matcher = pattern.matcher(s);

        boolean found = matcher.find();
        if (found) {
            System.out.println(matcher.start() + ", " + matcher.end());
        }

    }

    protected void onOk() {
        if (operation == OPERATION_FIND) {
            find(false);
        } else {
            replace(false);
        }
    }

}