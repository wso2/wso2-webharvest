package org.webharvest.gui.component;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Common scroll pane used in application.
 */
public class WHScrollPane extends JScrollPane {

    public WHScrollPane(Component view, int vsbPolicy, int hsbPolicy) {
        super(view, vsbPolicy, hsbPolicy);
        defineUI();
    }

    public WHScrollPane(Component view) {
        super(view);
        defineUI();
    }

    public WHScrollPane(int vsbPolicy, int hsbPolicy) {
        super(vsbPolicy, hsbPolicy);
        defineUI();
    }

    public WHScrollPane() {
        defineUI();
    }

    private void defineUI() {
        setBorder(new EmptyBorder(0, 0, 0, 0));
    }
    
}