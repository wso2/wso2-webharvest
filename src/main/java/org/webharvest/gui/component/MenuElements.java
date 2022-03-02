package org.webharvest.gui.component;

import javax.swing.*;
import java.awt.*;

/**
 * Elements used for menu creation.
 */
public class MenuElements {

    public static class Menu extends JMenu {
        public Menu(String s) {
            super(s);
            setPopupMenuLook(getPopupMenu());
        }

        public void addSeparator() {
            JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
            getPopupMenu().add(separator);
        }
    }


    public static class InnerMenu extends JMenu {
        public InnerMenu(String s) {
            super(s);

            setOpaque(false);
            setPopupMenuLook(getPopupMenu());
        }

        public void addSeparator() {
            JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
            getPopupMenu().add(separator);
        }
    }

    public static class MenuItem extends JMenuItem {
        public MenuItem(String s) {
            super(s);
            defineLook();
        }
        public MenuItem(String text, int mnemonic) {
            super(text, mnemonic);
            defineLook();
        }
        public MenuItem(String text, Icon icon) {
            super(text, icon);
            defineLook();
        }
        private void defineLook() {
        }
    }

    public static class RadioMenuItem extends JRadioButtonMenuItem {
        public RadioMenuItem(String text) {
            super(text);
        }
    }

    public static class CheckboxMenuItem extends JCheckBoxMenuItem {
        public CheckboxMenuItem(String text, boolean isChecked) {
            super(text, isChecked);
            setOpaque(false);
        }
    }

    public static void setPopupMenuLook(final JPopupMenu popup) {
    }

}