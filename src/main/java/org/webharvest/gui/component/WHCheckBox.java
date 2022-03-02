package org.webharvest.gui.component;

import org.webharvest.gui.*;

import javax.swing.*;
import java.awt.*;

/**
 * Radio buttons used throughout LiveCharts.
 */
public class WHCheckBox extends JCheckBox {

    public WHCheckBox() {
        defineLook();
    }

    public WHCheckBox(String text) {
        super(text);
        defineLook();
    }

    public WHCheckBox(String text, boolean selected) {
        super(text, selected);
        defineLook();
    }

    private void defineLook() {
//        setOpaque(false);
//        setIcon(ResourceManager.CHECKBOX_ICON);
//        setSelectedIcon(ResourceManager.CHECKBOX_SELECTED_ICON);
    }

    public Dimension getPreferredSize() {
        return new Dimension(super.getPreferredSize().width, 16);
    }

}