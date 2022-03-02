package org.webharvest.gui.component;

import javax.swing.*;
import java.awt.*;

/**
 * @author: Vladimir Nikic
 * Date: Jul 4, 2007
 */
public class ProportionalSplitPane extends JSplitPane {

    private boolean isPainted = false;
    private boolean hasProportionalLocation = true;
    private double proportionalLocation;

    public ProportionalSplitPane(int type) {
        super(type);
    }

    public void setDividerLocation(double proportionalLocation) {
        if (!isPainted) {
            hasProportionalLocation = true;
            this.proportionalLocation = proportionalLocation;
        } else {
            super.setDividerLocation(proportionalLocation);
        }
    }

    public void paint(Graphics g) {
        if (!isPainted) {
            if (hasProportionalLocation) {
                super.setDividerLocation(proportionalLocation);
            }
            isPainted = true;
        }
        super.paint(g);
    }

}