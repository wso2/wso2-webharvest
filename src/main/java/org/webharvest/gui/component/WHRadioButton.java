package org.webharvest.gui.component;

import org.webharvest.gui.ResourceManager;

import javax.swing.*;
import java.awt.*;

/**
 * Radio buttons used throughout application.
 */
public class WHRadioButton extends JRadioButton {

    public WHRadioButton() {
        defineLook();
    }

    public WHRadioButton(String text) {
        super(text);
        defineLook();
    }

    public WHRadioButton(String text, boolean selected) {
        super(text, selected);
        defineLook();
    }

    private void defineLook() {
//        setOpaque(false);
//        setIcon(ResourceManager.RADIO_BUTTON_ICON);
//        setSelectedIcon(ResourceManager.RADIO_BUTTON_SELECTED_ICON);
    }

    public Dimension getPreferredSize() {
        return new Dimension(super.getPreferredSize().width, 16);
    }

}