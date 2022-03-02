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

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Map;
import java.util.Iterator;

/**
 * @author: Vladimir Nikic
 * Date: May 7, 2007
 */
public class PropertiesGridModel extends AbstractTableModel implements TableModelListener {

    private Object properties[][];
    private TreeNodeInfo nodeInfo;

    public PropertiesGridModel() {
        addTableModelListener(this);
    }

    public Class getColumnClass(int columnIndex) {
        return columnIndex == 0 ? String.class : Object.class;
    }

    public int getRowCount() {
        return this.properties == null ? 0 : this.properties.length;
    }

    public int getColumnCount() {
        return 3;
    }

    public String getColumnName(int i) {
        if (i == 0) {
            return "Name";
        } else if (i == 1) {
            return "Value";
        }

        return null;
    }

    public boolean isCellEditable(int i, int j) {
        return j >= 1;
    }

    public Object getValueAt(int i, int j) {
        return j <= 1 ? this.properties[i][j] : null;
    }

    public void tableChanged(TableModelEvent tablemodelevent) {
    }

    public void setProperties(Map properties, TreeNodeInfo nodeInfo) {
        this.nodeInfo = nodeInfo;

        if (properties != null) {
            this.properties = new Object[properties.size()][2];
            Iterator iterator = properties.entrySet().iterator();
            int index = 0;
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                this.properties[index][0] = entry.getKey();
                this.properties[index][1] = entry.getValue();
                index++;
            }
        } else {
            this.properties = null;
        }
        
        fireTableDataChanged();
    }

    public TreeNodeInfo getNodeInfo() {
        return nodeInfo;
    }
    
}