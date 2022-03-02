package org.webharvest.gui.component;

import javax.swing.border.*;
import java.awt.*;

/**
 * Titled border used throughout the application.
 */
public class WHTitledBorder extends TitledBorder {

    public WHTitledBorder() {
        this("");
    }

    public WHTitledBorder(String title) {
        super(new EtchedBorder(EtchedBorder.LOWERED), title);
        setTitleColor(Color.black);
        setTitleFont(new Font("Tahoma", Font.PLAIN, 11));
    }

}