package org.webharvest.gui.component;

import org.webharvest.gui.*;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Common dilaog containing OK, Cancel buttons
 */
abstract public class CommonDialog extends JDialog {

    private Map components = new HashMap();
    private DefaultKeyboardFocusManager keyManager;
    private Component lastFocusOwner;

    public CommonDialog() {
        super(GuiUtils.getActiveFrame());
        lastFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        setResizable(false);
        setModal(true);

        keyManager = new DefaultKeyboardFocusManager() {
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    int keyCode = e.getKeyCode();
                    Object source = e.getSource();
                    if (keyCode == KeyEvent.VK_ESCAPE || keyCode == KeyEvent.VK_ENTER) {
                        if (source instanceof JTextArea && keyCode == KeyEvent.VK_ENTER) {
                            return false;
                        }
                        JComboBox comboBox = source instanceof Component ? getParentComboBox((Component)source) : null;
                        if (comboBox != null && comboBox.isPopupVisible()) {
                            return false;
                        } else if (isActive()) {
                            if (keyCode == KeyEvent.VK_ESCAPE) {
                                onCancel();
                                return true;
                            } else {
                                onOk();
                            }
                        }
                    }
                }
                return false;
            }
        };
    }

    private JComboBox getParentComboBox(Component comp) {
        do {
            if (comp instanceof JComboBox) {
                return (JComboBox) comp;
            }
            comp = comp.getParent();
        } while (comp != null);
        return null;
    }

    public CommonDialog(String title) {
        this();
        this.setTitle(title);
    }

    public void setVisible(boolean b) {
        KeyboardFocusManager keyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        if (b) {
            keyboardFocusManager.addKeyEventDispatcher(keyManager);
        } else {
            keyboardFocusManager.removeKeyEventDispatcher(keyManager);
        }
        super.setVisible(b);

        Window owner = getOwner();
        if (owner != null) {
            owner.requestFocus();
            if (lastFocusOwner != null) {
                lastFocusOwner.requestFocusInWindow();
            }
        }
    }

    protected JButton createOkButton() {
        FixedSizeButton okButton = new FixedSizeButton("OK", 80, -1);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOk();
            }
        });
        return okButton;
    }

    protected JButton createCancelButton() {
        FixedSizeButton cancelButton = new FixedSizeButton("Cancel", 80, -1);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });
        return cancelButton;
    }

    protected void onCancel() {
        setVisible(false);
    }

    abstract protected void onOk();

    public void addComponent(String id, Component comp) {
        if (id != null) {
            components.put(id, comp);
        }
    }

    public Component getComponent(String id) {
        return id != null ? (Component) components.get(id) : null;
    }

    public Object getComponentValue(String componentId) {
        Object comp = components.get(componentId);
        if (comp != null) {
            if (comp instanceof JTextComponent) {
                return ((JTextComponent)comp).getText();
            } else if (comp instanceof JComboBox) {
                return ((JComboBox)comp).getSelectedItem();
            }
        }

        return null;
    }

    public boolean getBooleanValue(String componentId) {
        Object comp = components.get(componentId);
        if (comp != null) {
            if (comp instanceof JRadioButton) {
                return ((JRadioButton)comp).isSelected();
            } else if (comp instanceof JCheckBox) {
                return ((JCheckBox)comp).isSelected();
            }
        }

        return false;
    }

    public JTextField getTextField(String name) {
        Object comp = components.get(name);
        return comp instanceof JTextField ? (JTextField)comp : null;
    }

    public JTextArea getTextArea(String name) {
        Object comp = components.get(name);
        return comp instanceof JTextArea ? (JTextArea)comp : null;
    }

    public JComboBox getComboBox(String name) {
        Object comp = components.get(name);
        return comp instanceof JComboBox ? (JComboBox)comp : null;
    }

    public JList getList(String name) {
        Object comp = components.get(name);
        return comp instanceof JList ? (JList)comp : null;
    }

    public JCheckBox getCheckBox(String name) {
        Object comp = components.get(name);
        return comp instanceof JCheckBox ? (JCheckBox)comp : null;
    }

    public JRadioButton getRadioButton(String name) {
        Object comp = components.get(name);
        return comp instanceof JRadioButton ? (JRadioButton) comp : null;
    }

    public JLabel getLabel(String name) {
        Object comp = components.get(name);
        return comp instanceof JLabel ? (JLabel)comp : null;
    }

    public JButton getButton(String name) {
        Object comp = components.get(name);
        return comp instanceof JButton ? (JButton)comp : null;
    }

    public JSlider getSlider(String name) {
        Object comp = components.get(name);
        return comp instanceof JSlider ? (JSlider)comp : null;
    }
    
}