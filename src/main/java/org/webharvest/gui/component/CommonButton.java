package org.webharvest.gui.component;

import org.webharvest.gui.ui.*;

import javax.swing.*;
import java.awt.*;

public class CommonButton extends JButton {

    private WHButtonUI whButtonUI = new WHButtonUI();

    public CommonButton() {
        defineLook();
    }

    public CommonButton(String text) {
        super(text);
        defineLook();
    }

    public CommonButton(String text, Icon icon) {
        super(text, icon);
        defineLook();
    }

    public CommonButton(Icon icon) {
        super(icon);
        defineLook();
    }

    public CommonButton(AbstractAction action) {
        super(action);
        defineLook();
    }

    private void defineLook() {
        setUI(whButtonUI);
    }

    public boolean isShowDownArrow() {
        return whButtonUI.isShowDownArrow();
    }

}