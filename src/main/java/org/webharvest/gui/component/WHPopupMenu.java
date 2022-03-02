package org.webharvest.gui.component;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

/**
 * Common popup menu.
 */
public class WHPopupMenu extends JPopupMenu {

    // single currently opened popup menu
    private static WHPopupMenu openPopup = null;

    public static WHPopupMenu getOpenPopup() {
        return openPopup;
    }

    private Map submenus = new LinkedHashMap();

    public WHPopupMenu() {
        addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                openPopup = WHPopupMenu.this;
            }
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                openPopup = null;
            }
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });

        MenuElements.setPopupMenuLook(this);
    }

    public void show(Component invoker, int x, int y) {
        int lastCompIndex = getComponentCount() - 1;
        if (lastCompIndex >= 0) {
            Component lastComp = getComponent(lastCompIndex);
            if (lastComp instanceof JSeparator) {
                remove(lastCompIndex);
            }
        }
        super.show(invoker, x, y);
    }

    public void addSeparator() {
        int count = getComponentCount();
        if (count == 0) {
            return;
        }

        int lastCompIndex = count - 1;
        if (lastCompIndex >= 0) {
            Component lastComp = getComponent(lastCompIndex);
            if (lastComp instanceof JSeparator) {
                return;
            }
        }
        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        add(separator);
    }
    
}