package org.webharvest.gui.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * LC look & feel text field
 */
public class WHTextField extends JTextField {

    public WHTextField() {
        super();
        define();
    }

    public WHTextField(int columns) {
        super(columns);
        define();
    }

    public WHTextField(String text) {
        super(text);
        define();
    }

    public WHTextField(String text, int columns) {
        super(text, columns);
        define();
    }

    private void define() {
//        setBorder( new WHControlsBorder() );
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    onEnterPressed();
                }
            }
        });
    }

    public void onEnterPressed() {
        // do nothing - left to ancestors
    }

    public Dimension getPreferredSize() {
        Dimension preferredSize = super.getPreferredSize();
//        return new Dimension(preferredSize.width, 16);
        return preferredSize;
    }

}