package org.webharvest.gui.ui;

import javax.swing.plaf.basic.*;
import javax.swing.plaf.*;
import javax.swing.*;
import java.awt.*;

public class WHScrollBarUI extends BasicScrollBarUI {

    public void installUI(JComponent c) {
        super.installUI(c);
        if (scrollbar != null) {
            scrollbar.setPreferredSize(scrollbar.getOrientation() == SwingConstants.HORIZONTAL ? new Dimension(1, 14) : new Dimension(14, 1));
        }
    }

    public static ComponentUI createUI(JComponent c) {
        return new WHScrollBarUI();
    }

    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        int w = c.getWidth();
        int h = c.getHeight();
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = scrollbar.getOrientation() == SwingConstants.VERTICAL ?
                new GradientPaint(0, 0, new Color(215, 212, 207), w - 1, 0, new Color(234, 232, 228), false) :
                new GradientPaint(0, 0, new Color(215, 212, 207), 0, h - 1, new Color(234, 232, 228), false);
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        int w = thumbBounds.width;
        int h = thumbBounds.height;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = scrollbar.getOrientation() == SwingConstants.VERTICAL ?
                new GradientPaint(1, 0, new Color(212, 208, 200), w - 1, 0, new Color(187, 182, 171), true) :
                new GradientPaint(0, 1, new Color(212, 208, 200), 0, h - 1, new Color(187, 182, 171), true);
        g2.setPaint(gp);
        g2.fillRect(thumbBounds.x, thumbBounds.y, w - 1, h - 1);
        g2.setColor(new Color(126, 124, 120));
        g2.drawRoundRect(thumbBounds.x, thumbBounds.y, w - 1, h - 1, 4, 4);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        if (scrollbar.getOrientation() == SwingConstants.VERTICAL) {
            int hh = thumbBounds.y + thumbBounds.height / 2 - 1;
            g.setColor(new Color(182, 179, 171));
            g.drawLine(4, hh, 8, hh);
            g.drawLine(4, hh + 3, 8, hh + 3);
            g.drawLine(4, hh - 3, 8, hh - 3);
            g.setColor(new Color(229, 227, 222));
            g.drawLine(5, hh + 1, 9, hh + 1);
            g.drawLine(5, hh + 4, 9, hh + 4);
            g.drawLine(5, hh - 2, 9, hh - 2);
        } else {
            int ww = thumbBounds.x + thumbBounds.width / 2 - 1;
            g.setColor(new Color(182, 179, 171));
            g.drawLine(ww, 4, ww, 8);
            g.drawLine(ww + 3, 4, ww + 3, 8);
            g.drawLine(ww - 3, 4, ww - 3, 8);
            g.setColor(new Color(229, 227, 222));
            g.drawLine(ww + 1, 5, ww + 1, 9);
            g.drawLine(ww + 4, 5, ww + 4, 9);
            g.drawLine(ww - 2, 5, ww - 2, 9);
        }
    }

    private JButton createButton(final boolean isDecrease) {
        JButton button = new JButton() {
            public void paint(Graphics g) {
                int w = getWidth();
                int h = getHeight();
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = scrollbar.getOrientation() == SwingConstants.VERTICAL ?
                        new GradientPaint(1, 0, new Color(187, 182, 171), w - 1, 0, new Color(212, 208, 200), true) :
                        new GradientPaint(0, 1, new Color(187, 182, 171), 0, h - 1, new Color(212, 208, 200), true);
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);
                g.setColor(new Color(85, 83, 79));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 4, 4);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g.setColor(Color.black);
                if (scrollbar.getOrientation() == SwingConstants.VERTICAL) {
                    if (isDecrease) {
                        g.drawLine(7, 5, 7, 5);
                        g.drawLine(6, 6, 8, 6);
                        g.drawLine(5, 7, 9, 7);
                        g.drawLine(4, 8, 10, 8);
                    } else {
                        g.drawLine(7, 8, 7, 8);
                        g.drawLine(6, 7, 8, 7);
                        g.drawLine(5, 6, 9, 6);
                        g.drawLine(4, 5, 10, 5);
                    }
                } else {
                    if (isDecrease) {
                        g.drawLine(5, 7, 5, 7);
                        g.drawLine(6, 6, 6, 8);
                        g.drawLine(7, 5, 7, 9);
                        g.drawLine(8, 4, 8, 10);
                    } else {
                        g.drawLine(8, 7, 8, 7);
                        g.drawLine(7, 6, 7, 8);
                        g.drawLine(6, 5, 6, 9);
                        g.drawLine(5, 4, 5, 10);
                    }
                }
            }
        };
        button.setPreferredSize(new Dimension(14, 14));
        return button;
    }

    protected JButton createDecreaseButton(int orientation) {
        return createButton(true);
    }

    protected JButton createIncreaseButton(int orientation) {
        return createButton(false);
    }

    protected Dimension getMinimumThumbSize() {
        return new Dimension(14, 14);
    }

}