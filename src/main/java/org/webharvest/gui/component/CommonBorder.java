package org.webharvest.gui.component;

import javax.swing.border.Border;
import java.awt.*;

/**
 * Rounded border for most of the controls.
 */
public class CommonBorder implements Border {

    private static int NORMAL_STATUS = 0;
    private static int MOUSEOVER_STATUS = 1;

    private int status = NORMAL_STATUS;

    public Insets getBorderInsets(Component c) {
        return new Insets(2, 2, 2, 2);
    }

    public boolean isBorderOpaque() {
        return true;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(status == MOUSEOVER_STATUS ? new Color(176, 175, 35) : new Color(220, 220, 220));
        g2.drawRect(1, 1, width - 3, height - 3);
        g2.setColor( new Color(85, 83, 79) );
        g2.drawRoundRect(0, 0, width - 1, height - 1, 4, 4);
    }

    public void setNormalStatus() {
        this.status = NORMAL_STATUS;
    }

    public void setMouseOverStatus() {
        this.status = MOUSEOVER_STATUS;
    }

}