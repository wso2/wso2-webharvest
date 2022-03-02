package org.webharvest.gui.component;

import org.webharvest.gui.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Dialog which offers user to insert text value
 */
public class InputDialog extends CommonDialog {

    private Component caller;
    private String result = null;
    private WHTextField inputField;

    public InputDialog(Component caller, String title, String message, String value, int textFieldSize, Icon icon) {
        super(title);
        this.caller = caller;

        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel(message);
        label.setIcon(icon);
        inputField = new WHTextField(value, textFieldSize);
        inputField.selectAll();

        panel.add(label, BorderLayout.WEST);
        panel.add(inputField, BorderLayout.CENTER);

        contentPane.add(panel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 4));
        buttonPanel.add(createOkButton());
        buttonPanel.add(createCancelButton());

        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        pack();
    }

    protected void onOk() {
        if (!"".equals(inputField.getText().trim())) {
            result = inputField.getText();
            setVisible(false);
        } else {
            inputField.requestFocus();
        }
    }

    public void setVisible(boolean b) {
        if (b) {
            Component relativeTo = caller;
            if (relativeTo == null) {
                relativeTo = GuiUtils.getActiveFrame();
            }
            if (relativeTo != null) {
                GuiUtils.centerRelativeTo(this, relativeTo);
            }
        }
        super.setVisible(b);
    }

    public String getValue() {
        setVisible(true);
        return result;
    }

}