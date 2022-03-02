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

import org.webharvest.gui.component.*;
import org.webharvest.definition.DefinitionResolver;
import org.webharvest.exception.PluginException;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class SettingsDialog extends CommonDialog implements ChangeListener {

    /**
     * Class definining elements in list of plugins. Each item has name (fully qualified class name)
     * and optional error message telling why plugin cannot be registered.
     */
    private class PluginListItem {
        String className;
        String errorMessage;

        private PluginListItem(String className, String errorMessage) {
            this.className = className;
            this.errorMessage = errorMessage;
        }

        private boolean isValid() {
            return errorMessage == null;
        }

        public String toString() {
            return className;
        }
    }

    /**
     * List model implementation for the list of plugins.
     */
    private class PluginListModel extends DefaultListModel {
        public void addElement(Object obj, boolean throwErrIfRegistered) {
            SettingsDialog.PluginListItem pluginListItem = createItem(obj, throwErrIfRegistered);
            if (pluginListItem != null) {
                super.addElement(pluginListItem);
            }
        }

        public boolean setElement(Object obj, int index) {
            SettingsDialog.PluginListItem pluginListItem = createItem(obj, true);
            if (pluginListItem != null) {
                super.setElementAt(pluginListItem, index);
                fireContentsChanged(this, index, index);
                return true;
            }
            return false;
        }

        private PluginListItem createItem(Object obj, boolean throwErrIfRegistered) {
            String newClassName = obj.toString();
            int size = getSize();
            // check if it already exists in the list
            for (int i = 0; i < size; i++) {
                PluginListItem item = (PluginListItem) get(i);
                if (item != null && item.className.equals(newClassName)) {
                    if (SettingsDialog.this.isVisible()) {
                        JOptionPane.showMessageDialog(SettingsDialog.this, "Plugin is already added to the list!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    return null;
                }
            }

            String errorMessage = null;

            if ( !DefinitionResolver.isPluginRegistered(newClassName) || throwErrIfRegistered ) {
                try {
                    DefinitionResolver.registerPlugin(newClassName);
                } catch (PluginException e) {
                    errorMessage = e.getMessage();
                }
            }

            return new PluginListItem(newClassName, errorMessage);            
        }
    }

    /**
     * Cell renderer for the list of plugins. It displays label with plugin class name
     * and OK or ERROR icon telling if plugin is registered successfully or not. If
     * plugin registration failed, tooltip is defined with error message for the label.
     */
    private class PluginListCellRenderer extends JLabel implements ListCellRenderer {
        private PluginListCellRenderer() {
            setOpaque(true);
            setPreferredSize(new Dimension(1, 18));
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            PluginListItem item = (PluginListItem) value;
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            setText(item.className);
            setIcon(item.isValid() ? ResourceManager.VALID_ICON : ResourceManager.INVALID_ICON);
            setToolTipText(item.errorMessage);
            return this;
        }
    }

    // Ide instance where this dialog belongs.
    private Ide ide;

    // settings fields
    private JTextField workingPathField;
    private JComboBox fileCharsetComboBox;
    private JTextField proxyServerField;
    private JTextField proxyPortField;
    private JTextField proxyUsernameField;
    private JTextField proxyPasswordField;
    private JTextField ntlmHostField;
    private JTextField ntlmDomainField;
    private JCheckBox proxyEnabledCheckBox;
    private JCheckBox proxyAuthEnabledCheckBox;
    private JCheckBox ntlmEnabledCheckBox;

    private JLabel proxyUsernameLabel;
    private JLabel proxyPasswordLabel;
    private JLabel proxyPortLabel;
    private JLabel proxyServerLabel;
    private JLabel ntlmHostLabel;
    private JLabel ntlmDomainLabel;

    private JCheckBox showHierarchyByDefaultCheckBox;
    private JCheckBox showLogByDefaultCheckBox;
    private JCheckBox showLineNumbersByDefaultCheckBox;
    private JCheckBox dynamicConfigLocateCheckBox;
    private JCheckBox showFinishDialogCheckBox;

    private JButton pluginAddButton;
    private JButton pluginEditButton;
    private JButton pluginRemoveButton;
    private PluginListModel pluginListModel;
    private JList pluginsList;

    private final JFileChooser pathChooser = new JFileChooser();

    public SettingsDialog(Ide ide) throws HeadlessException {
        super("Settings");
        this.ide = ide;
        this.setResizable(false);

        pathChooser.setFileFilter( new FileFilter() {
            public boolean accept(File f) {
                return f.exists() && f.isDirectory();
            }
            public String getDescription() {
                return "All directories";
            }
        });
        pathChooser.setMultiSelectionEnabled(false);
        pathChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        createGUI();
    }

    private void createGUI() {
        Container contentPane = this.getContentPane();

        JPanel generalPanel = new JPanel(new GridBagLayout());
        generalPanel.setBorder( new EmptyBorder(4, 4, 4, 4) );

        contentPane.setLayout( new BorderLayout() );

        JTabbedPane tabbedPane = new JTabbedPane();

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(2, 5, 2, 5);

        workingPathField = new FixedSizeTextField(250, -1);

        Map charsetsMap = Charset.availableCharsets();
        Vector allSupportedCharsets = new Vector(charsetsMap.keySet());
        fileCharsetComboBox = new WHComboBox(allSupportedCharsets);

        proxyServerField = new FixedSizeTextField(250, -1);
        proxyPortField = new FixedSizeTextField(250, -1);
        proxyUsernameField = new FixedSizeTextField(250, -1);
        proxyPasswordField = new FixedSizeTextField(250, -1);
        ntlmHostField = new FixedSizeTextField(250, -1);
        ntlmDomainField = new FixedSizeTextField(250, -1);

        proxyEnabledCheckBox = new WHCheckBox("Proxy server enabled");
        proxyEnabledCheckBox.addChangeListener(this);
        proxyAuthEnabledCheckBox = new WHCheckBox("Proxy authentication enabled");
        proxyAuthEnabledCheckBox.addChangeListener(this);
        ntlmEnabledCheckBox = new WHCheckBox("Use NTLM authentication scheme");
        ntlmEnabledCheckBox.addChangeListener(this);

        constraints.gridx = 0;
        constraints.gridy = 0;
        generalPanel.add( new JLabel("Output path"), constraints );

        constraints.gridx = 1;
        constraints.gridy = 0;
        JPanel pathPanel = new JPanel( new FlowLayout(FlowLayout.LEFT, 0, 0) );
        pathPanel.add(workingPathField);
        JButton chooseDirButton = new SmallButton("...") {
            public Dimension getPreferredSize() {
                Dimension preferredSize = super.getPreferredSize();
                return new Dimension(30, workingPathField.getHeight());
            }
        };
        chooseDirButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int returnVal = pathChooser.showOpenDialog(SettingsDialog.this);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    File selectedDir = pathChooser.getSelectedFile();
                    if (selectedDir != null) {
                        workingPathField.setText( selectedDir.getAbsolutePath() );
                    }
                }
            }
        });
        pathPanel.add(chooseDirButton);
        generalPanel.add(pathPanel, constraints );

        constraints.gridx = 0;
        constraints.gridy = 1;
        generalPanel.add( new JLabel("File encoding"), constraints );

        constraints.gridx = 1;
        constraints.gridy = 1;
        generalPanel.add(fileCharsetComboBox, constraints );


        constraints.gridx = 0;
        constraints.gridy = 2;
        generalPanel.add(proxyEnabledCheckBox , constraints );

        constraints.gridx = 0;
        constraints.gridy = 3;
        proxyServerLabel = new JLabel("Proxy server");
        generalPanel.add(proxyServerLabel, constraints );

        constraints.gridx = 1;
        constraints.gridy = 3;
        generalPanel.add(proxyServerField, constraints );

        constraints.gridx = 0;
        constraints.gridy = 4;
        proxyPortLabel = new JLabel("Proxy port (blank is default)");
        generalPanel.add(proxyPortLabel, constraints );

        constraints.gridx = 1;
        constraints.gridy = 4;
        generalPanel.add(proxyPortField, constraints );

        constraints.gridx = 0;
        constraints.gridy = 5;
        generalPanel.add(proxyAuthEnabledCheckBox , constraints );

        constraints.gridx = 0;
        constraints.gridy = 6;
        proxyUsernameLabel = new JLabel("Proxy username");
        generalPanel.add(proxyUsernameLabel, constraints );

        constraints.gridx = 1;
        constraints.gridy = 6;
        generalPanel.add(proxyUsernameField, constraints );

        constraints.gridx = 0;
        constraints.gridy = 7;
        proxyPasswordLabel = new JLabel("Proxy password");
        generalPanel.add(proxyPasswordLabel, constraints );

        constraints.gridx = 1;
        constraints.gridy = 7;
        generalPanel.add(proxyPasswordField, constraints );

        constraints.gridx = 0;
        constraints.gridy = 8;
        generalPanel.add(ntlmEnabledCheckBox , constraints );

        constraints.gridx = 0;
        constraints.gridy = 9;
        ntlmHostLabel = new JLabel("NT host");
        generalPanel.add(ntlmHostLabel, constraints );

        constraints.gridx = 1;
        constraints.gridy = 9;
        generalPanel.add(ntlmHostField, constraints );

        constraints.gridx = 0;
        constraints.gridy = 10;
        ntlmDomainLabel = new JLabel("NT domain");
        generalPanel.add(ntlmDomainLabel, constraints );

        constraints.gridx = 1;
        constraints.gridy = 10;
        generalPanel.add(ntlmDomainField, constraints );

        JPanel buttonPanel = new JPanel( new FlowLayout(FlowLayout.CENTER) );
        buttonPanel.add(createOkButton());
        buttonPanel.add(createCancelButton());

        JPanel viewPanel = new JPanel();
        viewPanel.setBorder( new EmptyBorder(4, 4, 4, 4) );
        viewPanel.setLayout( new BoxLayout(viewPanel, BoxLayout.PAGE_AXIS) );
        this.showHierarchyByDefaultCheckBox = new WHCheckBox("Show hierarchy panel by default");
        this.showLogByDefaultCheckBox = new WHCheckBox("Show log panel by default");
        this.showLineNumbersByDefaultCheckBox = new WHCheckBox("Show line numbers by default");
        this.dynamicConfigLocateCheckBox = new WHCheckBox("Dynamically locate processors in runtime");
        this.showFinishDialogCheckBox = new WHCheckBox("Show info/error dialog when execution finishes");

        viewPanel.add(this.showHierarchyByDefaultCheckBox);
        viewPanel.add(this.showLogByDefaultCheckBox);
        viewPanel.add(this.showLineNumbersByDefaultCheckBox);
        viewPanel.add(this.dynamicConfigLocateCheckBox);
        viewPanel.add(this.showFinishDialogCheckBox);

        JPanel pluginsPanel = new JPanel(new BorderLayout(5, 0));
        JPanel pluginButtonsPanel = new JPanel();
        SpringLayout springLayout = new SpringLayout();
        pluginButtonsPanel.setLayout(springLayout);
        pluginButtonsPanel.setBorder(new EmptyBorder(3, 0, 3, 3));
        pluginButtonsPanel.setPreferredSize(new Dimension(116, 1));

        final String pluginInputMsg = "Full class name of the plugin";

        pluginAddButton = new FixedSizeButton("Add plugin", 110, 22);
        pluginAddButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String className = new InputDialog(SettingsDialog.this, "Input", pluginInputMsg, "", 50, null).getValue();
                if (className != null) {
                    pluginListModel.addElement(className, true);
                    pluginsList.setSelectedIndex(pluginListModel.size() - 1);
                }
            }
        });
        pluginEditButton = new FixedSizeButton("Edit plugin", 110, 22);
        final ActionListener editListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int index = pluginsList.getSelectedIndex();
                if (index >= 0) {
                    String oldClassName = pluginsList.getSelectedValue().toString();
                    String className = new InputDialog(SettingsDialog.this, "Input", pluginInputMsg, oldClassName, 50, null).getValue();
                    if ( className != null && !className.equals(oldClassName) ) {
                        boolean isSet = pluginListModel.setElement(className, index);
                        if (isSet) {
                            DefinitionResolver.unregisterPlugin(oldClassName);
                        }
                    }
                }
            }
        };
        pluginEditButton.addActionListener(editListener);
        pluginRemoveButton = new FixedSizeButton("Remove plugin", 110, 22);
        pluginRemoveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int index = pluginsList.getSelectedIndex();
                if (index >= 0) {
                    String oldClassName = pluginsList.getSelectedValue().toString();
                    DefinitionResolver.unregisterPlugin(oldClassName);
                    pluginListModel.remove(index);
                    pluginsList.setSelectedIndex(Math.min(index, pluginListModel.size() - 1));
                }
            }
        });

        pluginButtonsPanel.add(pluginAddButton);
        pluginButtonsPanel.add(pluginEditButton);
        pluginButtonsPanel.add(pluginRemoveButton);

        springLayout.putConstraint(SpringLayout.NORTH, pluginEditButton, 5, SpringLayout.SOUTH, pluginAddButton);
        springLayout.putConstraint(SpringLayout.NORTH, pluginRemoveButton, 5, SpringLayout.SOUTH, pluginEditButton);
        
        pluginsPanel.add(pluginButtonsPanel, BorderLayout.EAST);
        JPanel pluginsListPanel = new JPanel(new BorderLayout(5, 5));
        pluginsListPanel.setBorder(new EmptyBorder(3, 3, 3, 3));

        pluginListModel = new PluginListModel();
        pluginsList = new WHList(pluginListModel);
        pluginsList.setCellRenderer(new PluginListCellRenderer());
        pluginsList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                updateControls();
            }
        });
        pluginsList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    if (pluginsList.getSelectedIndex() >= 0) {
                        editListener.actionPerformed(null);
                    }
                }
            }
        });

        pluginsListPanel.add(new WHScrollPane(pluginsList), BorderLayout.CENTER);
        pluginsPanel.add(pluginsListPanel, BorderLayout.CENTER);

        tabbedPane.addTab("General", null, generalPanel, null);
        tabbedPane.addTab("View", null, viewPanel, null);
        tabbedPane.addTab("Plugins", null, pluginsPanel, null);

        contentPane.add(tabbedPane, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        updateControls();

        this.pack();
    }

    private void fillValues() {
        Settings settings = ide.getSettings();

        workingPathField.setText( settings.getWorkingPath() );
        fileCharsetComboBox.setSelectedItem( settings.getFileCharset() );
        proxyServerField.setText( settings.getProxyServer() );
        proxyPortField.setText( settings.getProxyPort() > 0 ? "" + settings.getProxyPort() : "" );
        proxyUsernameField.setText( settings.getProxyUserename() );
        proxyPasswordField.setText( settings.getProxyPassword() );
        proxyEnabledCheckBox.setSelected( settings.isProxyEnabled() );
        proxyAuthEnabledCheckBox.setSelected( settings.isProxyAuthEnabled() );

        ntlmEnabledCheckBox.setSelected( settings.isNtlmAuthEnabled() );
        ntlmHostField.setText( settings.getNtlmHost() );
        ntlmDomainField.setText( settings.getNtlmDomain() );

        showHierarchyByDefaultCheckBox.setSelected( settings.isShowHierarchyByDefault() );
        showLogByDefaultCheckBox.setSelected( settings.isShowLogByDefault() );
        showLineNumbersByDefaultCheckBox.setSelected( settings.isShowLineNumbersByDefault() );
        dynamicConfigLocateCheckBox.setSelected( settings.isDynamicConfigLocate() );
        showFinishDialogCheckBox.setSelected( settings.isShowFinishDialog() );

        pluginListModel.clear();
        String[] plugins = settings.getPlugins();
        for (int i = 0; i < plugins.length; i++) {
            pluginListModel.addElement(plugins[i], false);
        }
    }

    private void undoPlugins() {
        Map externalPlugins = DefinitionResolver.getExternalPlugins();

        Settings settings = ide.getSettings();
        String[] plugins = settings.getPlugins();
        Set pluginSet = new HashSet();
        for (int i = 0; i < plugins.length; i++) {
            pluginSet.add(plugins[i]);
        }

        Set listSet = new HashSet();

        // unregister plugins registered during this settings session
        int count = pluginListModel.getSize();
        for (int i = 0; i < count; i++) {
            PluginListItem item = (PluginListItem) pluginListModel.get(i);
            listSet.add(item.className);
            if ( item.isValid() && !pluginSet.contains(item.className) ) {
                DefinitionResolver.unregisterPlugin(item.className);
            }
        }

        // register plugins unregistered during this setting session
        for (int i = 0; i < plugins.length; i++) {
            String currPlugin = plugins[i];
            if ( !listSet.contains(currPlugin) && !DefinitionResolver.isPluginRegistered(currPlugin) ) {
                try {
                    DefinitionResolver.registerPlugin(currPlugin);
                } catch (PluginException e) {
                    ; // do nothing - ignore
                }
            }
        }
    }

    public void setVisible(boolean b) {
        if (b) {
            fillValues();
        } else {
            undoPlugins();
        }
        super.setVisible(b);
    }

    private void define() {
        Settings settings = this.ide.getSettings();

        settings.setWorkingPath( this.workingPathField.getText() );
        settings.setFileCharset( this.fileCharsetComboBox.getSelectedItem().toString() );
        settings.setProxyServer( this.proxyServerField.getText() );

        int port = -1;
        try {
            port = Integer.parseInt( this.proxyPortField.getText() );
        } catch (NumberFormatException e) {
        }
        settings.setProxyPort(port);

        settings.setProxyUserename( this.proxyUsernameField.getText() );
        settings.setProxyPassword( this.proxyPasswordField.getText() );

        settings.setProxyEnabled( this.proxyEnabledCheckBox.isSelected() );
        settings.setProxyAuthEnabled( this.proxyAuthEnabledCheckBox.isSelected() );

        settings.setNtlmAuthEnabled( this.ntlmEnabledCheckBox.isSelected() );
        settings.setNtlmHost( this.ntlmHostField.getText() );
        settings.setNtlmDomain( this.ntlmDomainField.getText() );

        settings.setShowHierarchyByDefault(this.showHierarchyByDefaultCheckBox.isSelected());
        settings.setShowLogByDefault(this.showLogByDefaultCheckBox.isSelected());
        settings.setShowLineNumbersByDefault(this.showLineNumbersByDefaultCheckBox.isSelected());
        settings.setDynamicConfigLocate(this.dynamicConfigLocateCheckBox.isSelected());
        settings.setShowFinishDialog(this.showFinishDialogCheckBox.isSelected());

        int pluginCount = pluginListModel.getSize();
        String plugins[] = new String[pluginCount];
        for (int i = 0; i < pluginCount; i++) {
            PluginListItem item = (PluginListItem) pluginListModel.get(i);
            plugins[i] = item.className;
        }
        settings.setPlugins(plugins);

        try {
            settings.writeToFile();
        } catch (IOException e) {
            e.printStackTrace();
            GuiUtils.showErrorMessage("Error saving programs settings: " + e.getMessage());
        }

        updateControls();

        setVisible(false);
    }

    /**
     * Enable/disable controls depending on setting values.
     */
    private void updateControls() {
        boolean isProxyEnabled = this.proxyEnabledCheckBox.isSelected();
        boolean isProxyAuthEnabled = this.proxyAuthEnabledCheckBox.isSelected();
        boolean isNtlmAuthEnabled = this.ntlmEnabledCheckBox.isSelected();

        this.proxyServerLabel.setEnabled(isProxyEnabled);
        this.proxyServerField.setEnabled(isProxyEnabled);
        this.proxyPortLabel.setEnabled(isProxyEnabled);
        this.proxyPortField.setEnabled(isProxyEnabled);

        this.proxyAuthEnabledCheckBox.setEnabled(isProxyEnabled);

        this.proxyUsernameLabel.setEnabled( isProxyEnabled && isProxyAuthEnabled );
        this.proxyUsernameField.setEnabled( isProxyEnabled && isProxyAuthEnabled );
        this.proxyPasswordLabel.setEnabled( isProxyEnabled && isProxyAuthEnabled );
        this.proxyPasswordField.setEnabled( isProxyEnabled && isProxyAuthEnabled );

        this.ntlmEnabledCheckBox.setEnabled(isProxyEnabled && isProxyAuthEnabled);

        this.ntlmHostLabel.setEnabled( isProxyEnabled && isProxyAuthEnabled && isNtlmAuthEnabled );
        this.ntlmHostField.setEnabled( isProxyEnabled && isProxyAuthEnabled && isNtlmAuthEnabled );
        this.ntlmDomainLabel.setEnabled( isProxyEnabled && isProxyAuthEnabled && isNtlmAuthEnabled );
        this.ntlmDomainField.setEnabled( isProxyEnabled && isProxyAuthEnabled && isNtlmAuthEnabled );

        int selectedPluginIndex = pluginsList.getSelectedIndex();
        pluginEditButton.setEnabled( selectedPluginIndex >= 0 );
        pluginRemoveButton.setEnabled( selectedPluginIndex >= 0 );
    }

    public void stateChanged(ChangeEvent e) {
        updateControls();
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
                define();
            }
        };
        rootPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);


        return rootPane;
    }

    protected void onOk() {
        define();        
    }
    
}