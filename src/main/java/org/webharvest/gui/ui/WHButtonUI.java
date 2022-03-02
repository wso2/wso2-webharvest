package org.webharvest.gui.ui;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;

public class WHButtonUI extends BasicButtonUI {

    protected static final Color DISABLED_COLOR = new Color(125, 121, 111);

    private Component comp = null;

    private boolean showDownArrow = false;
    private boolean isDefaultColor = true;

    public static ComponentUI createUI(JComponent c) {
        return new WHButtonUI();
    }

    public void installUI(JComponent c) {
        this.comp = c;
        super.installUI(c);
    }

    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
    }

    protected void paintText(Graphics g, JComponent c, Rectangle textRect, String text) {
        AbstractButton b = (AbstractButton) c;
        ButtonModel model = b.getModel();
        FontMetrics fm = b.getFontMetrics(b.getFont());
        int mnemonicIndex = b.getDisplayedMnemonicIndex();
        int textShiftOffset = getTextShiftOffset();

        if (showDownArrow) {
            boolean textAdapted = false;
            do {
                int textWidth = SwingUtilities.computeStringWidth(fm, text);
                if (textWidth >= comp.getWidth() - 7 - textRect.x - textShiftOffset) {
                    if (text != null && text.endsWith("...")) {
                        text = text.substring(0, text.length() - 3);
                    }
                    if (text.length() > 0) {
                        text = text.substring(0, text.length() - 1);
                    } else {
                        text = "...";
                        textAdapted = true;
                    }
                    text += "...";
                } else {
                    textAdapted = true;
                }
            } while (!textAdapted);
        }

        // Draw the Text
        g.setColor(model.isEnabled() ? b.getForeground() : DISABLED_COLOR);
        g.drawString(text, textRect.x + textShiftOffset, textRect.y + fm.getAscent() + textShiftOffset);

        int w = c.getWidth();
        int h = c.getHeight();
        if (showDownArrow) {
            g.setColor(comp.isEnabled() ? Color.black : DISABLED_COLOR);
            int ha = h / 2;
            g.drawLine(w - 8, ha - 1, w - 4, ha - 1);
            g.drawLine(w - 7, ha, w - 5, ha);
            g.drawLine(w - 6, ha + 1, w - 6, ha + 1);
        }

    }

    public boolean isShowDownArrow() {
        return showDownArrow;
    }

    public void setShowDownArrow(boolean showDownArrow) {
        this.showDownArrow = showDownArrow;
    }

}