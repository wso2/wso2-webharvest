package org.webharvest.gui.component;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Combo boxes used in application
 */
public class WHList extends JList {

    {
        setBorder(new WHControlsBorder());
    }

    public WHList() {
        defineList();
    }

    public WHList(Object items[]) {
        defineList();
        addValues(items);
    }

    public WHList(Collection values) {
        defineList();
        addValues(values);
    }

    public WHList(ListModel dataModel) {
        super(dataModel);
    }

    private void defineList() {
        setModel(new DefaultListModel());
    }

    public void addValue(Object value) {
        if (value != null) {
            DefaultListModel model = (DefaultListModel) getModel();
            model.addElement(value);
        }
    }

    public void removeValue(Object value) {
        if (value != null) {
            DefaultListModel model = (DefaultListModel) getModel();
            model.removeElement(value);
        }
    }

    public void clearList() {
        DefaultListModel model = (DefaultListModel) getModel();
        model.removeAllElements();
    }

    public void addValues(Collection values) {
        DefaultListModel model = (DefaultListModel) getModel();
        if (values != null) {
            final Iterator iterator = values.iterator();
            while (iterator.hasNext()) {
                model.addElement(iterator.next() );
            }
        }
    }

    public void addValues(Object values[]) {
        DefaultListModel model = (DefaultListModel) getModel();
        for (int i = 0; i < values.length; i++) {
            model.addElement(values[i]);
        }
    }

    public Object[] getValues() {
        DefaultListModel model = (DefaultListModel) getModel();
        int size = model.getSize();
        Object result[] = new Object[size];
        for (int i = 0; i < size; i++) {
            result[i] = model.getElementAt(i);
        }

        return result;
    }

    public void insertValue(Object value, int index) {
        DefaultListModel model = (DefaultListModel) getModel();
        model.insertElementAt(value, index);
    }

    public int getModelSize() {
        DefaultListModel model = (DefaultListModel) getModel();
        return model.getSize();
    }

}