package org.webharvest.gui.component;

import javax.swing.*;
import java.awt.*;

public class FixedSizeButton extends CommonButton {

    public FixedSizeButton(int width, int height) {
        super();
        setFixedSize(width, height);
    }

    public FixedSizeButton(String text, int width, int height) {
        super(text);
        setFixedSize(width, height);
    }

    public FixedSizeButton(String text, Icon icon, int width, int height) {
        super(text, icon);
        setFixedSize(width, height);
    }

    public FixedSizeButton(Icon icon, int width, int height) {
        super(icon);
        setFixedSize(width, height);
    }

    private void setFixedSize(int width, int height) {
        if (height <= 0) {
            height = super.getPreferredSize().height;
        }
        Dimension dim = new Dimension(width, height);
        setPreferredSize(dim);
        setMinimumSize(dim);
        setMaximumSize(dim);
    }

}