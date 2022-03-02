package org.webharvest.gui.component;

import javax.swing.*;
import java.awt.*;

public class SmallButton extends CommonButton {

    public SmallButton() {
        super();
    }

    public SmallButton(String text) {
        super(text);
    }

    public SmallButton(String text, Icon icon) {
        super(text, icon);
    }

    public SmallButton(Icon icon) {
        super(icon);
    }

    public Dimension getPreferredSize() {
        Dimension preferredSize = super.getPreferredSize();
        return new Dimension(preferredSize.width + (isShowDownArrow() ? 18 : 6), preferredSize.height);
    }

}