package org.webharvest.gui.component;

import javax.swing.border.*;
import java.awt.*;

public class WHControlsBorder implements Border {

    private static final Color BORDER_COLOR = new Color(103, 101, 97);

    public WHControlsBorder() {
    }

    public Insets getBorderInsets(Component c) {
        return new Insets(2, 2, 2, 2);
    }

    public boolean isBorderOpaque() {
        return false;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(BORDER_COLOR);
        g2.drawRect(0, 0, width - 1, height - 1);
    }

}