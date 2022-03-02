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

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class RunParamsDialog extends JDialog {

    private class MyTableModel extends AbstractTableModel {
        public int getColumnCount() {
            return 2;
        }

        public int getRowCount() {
            return params.size();
        }

        public Object getValueAt(int row, int col) {
            String[] pair = (String[]) params.get(row);
            return pair[col];
        }

        public String getColumnName(int column) {
            return column == 0 ? "Name" : "Value";
        }

        public void removeRow(int rowIndex) {
            int size = params.size();
            if (rowIndex >= 0 && rowIndex < size) {
                params.remove(rowIndex);
                this.fireTableDataChanged();
            }
        }

        public void addEmptyRow() {
            params.add( new String[] {"", ""} );
            this.fireTableDataChanged();
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            if (rowIndex < getRowCount()) { 
                String rowArray[] = (String[]) params.get(rowIndex);
                rowArray[columnIndex] = (String) value;
            }
        }
    }

    // list of name-value pairs used as model for the table
    private java.util.List params = new ArrayList();

    // Ide instance where this dialog belongs.
    private Ide ide;

    // table data model
    private MyTableModel dataModel;

    // table with parameters
    private JTable table;

    public RunParamsDialog(Ide ide) throws HeadlessException {
        super(ide, "Initial Run Parameters", true);
        this.ide = ide;
        this.setResizable(false);

        createGUI();
    }

    private void createGUI() {
        Container contentPane = this.getContentPane();
        contentPane.setLayout( new BorderLayout() );

        this.dataModel = new MyTableModel();

        table = new JTable(this.dataModel) {
            public void editingStopped(ChangeEvent event) {
                TableCellEditor editor = (TableCellEditor) event.getSource();
                int row = getEditingRow();
                int column = getEditingColumn();
                if (row < 0) {
                    row = 0;
                }
                if (column < 0) {
                    column = 0;
                }
                String value = (String) editor.getCellEditorValue();
                dataModel.setValueAt(value, row, column);
                super.editingStopped(event);
            }


        };
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setReorderingAllowed(false);

        JPanel buttonPanel = new JPanel(new GridLayout(6, 1, 10, 5));
        buttonPanel.setBorder(new EmptyBorder(4, 2, 4, 4));

        JButton addButton = new CommonButton("Add");
        addButton.setMnemonic('A');
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dataModel.addEmptyRow();
                int lastRow = dataModel.getRowCount() - 1;
                table.grabFocus();
                table.getSelectionModel().setSelectionInterval(lastRow, lastRow);
                table.editCellAt(lastRow, 0);
            }
        });

        JButton removeButton = new CommonButton("   Remove   ");
        removeButton.setMnemonic('R');
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dataModel.removeRow(table.getSelectedRow());
            }
        });

        JButton okButton = new CommonButton("OK");
        okButton.setMnemonic('O');
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                defineParams();
                setVisible(false);
            }
        });

        JButton applyButton = new CommonButton("Apply");
        applyButton.setMnemonic('p');
        applyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                defineParams();
            }
        });

        JButton cancelButton = new CommonButton("Cancel");
        cancelButton.setMnemonic('C');
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(new JLabel(" "));
        buttonPanel.add(okButton);
        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);

        JPanel leftPane = new JPanel(new BorderLayout());
        leftPane.setBorder(new EmptyBorder(4, 4, 4, 4));
        JScrollPane tableScrollPane = new WHScrollPane(table);
        tableScrollPane.setBorder(new WHControlsBorder());
        leftPane.add(tableScrollPane, BorderLayout.CENTER);
        contentPane.add(leftPane, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.EAST);

        this.pack();
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

        return rootPane;
    }

    private void defineParams() {
        ConfigPanel configPanel = ide.getActiveConfigPanel();
        if (configPanel != null) {
            Map paramMap = new LinkedHashMap();
            Iterator iterator = params.iterator();
            while (iterator.hasNext()) {
                String[] pair = (String[]) iterator.next();
                if ( pair != null && pair[0] != null && !"".equals(pair[0].trim()) && pair[1] != null ) {
                    paramMap.put(pair[0], pair[1]);
                }
            }

            configPanel.setInitParams(paramMap);
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(400, 200);
    }

    public void setVisible(boolean b) {
        if (b) {
            ConfigPanel configPanel = ide.getActiveConfigPanel();
            if (configPanel != null) {
                params.clear();
                Map paramsMap = configPanel.getInitParams();
                if (paramsMap != null) {
                    Iterator iterator = paramsMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry entry = (Map.Entry) iterator.next();
                        params.add(new String[] {(String) entry.getKey(), (String) entry.getValue()});
                    }
                }
            }
            dataModel.fireTableDataChanged();
        }
        
        super.setVisible(b);
        if (b) {
            table.grabFocus();
        }
    }
    
}