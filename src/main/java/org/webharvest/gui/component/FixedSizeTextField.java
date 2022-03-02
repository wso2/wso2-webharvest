package org.webharvest.gui.component;

import javax.swing.*;
import java.awt.*;

/**
 * Text field with specified fixed dimension
 */
public class FixedSizeTextField extends WHTextField {

    private int width;
    private int height;

    public FixedSizeTextField(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public FixedSizeTextField(String text, int width, int height) {
        super(text);
        this.width = width;
        this.height = height;
    }

    public Dimension getPreferredSize() {
        return new Dimension(width, height<= 0 ? super.getPreferredSize().height : height);
    }

}