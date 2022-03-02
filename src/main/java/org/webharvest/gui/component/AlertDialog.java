package org.webharvest.gui.component;

import org.webharvest.gui.*;
import org.webharvest.utils.CommonUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog which displays alert message
 */
public class AlertDialog extends CommonDialog {

    private Component caller;
    private int result = JOptionPane.CANCEL_OPTION;

    public AlertDialog(String title, String message, Icon icon) {
        this(null, title, message, icon, new int[] {JOptionPane.OK_OPTION}, new String[] {"OK"});
    }

    public AlertDialog(Component caller, String title, String message, Icon icon, int options[], String buttLabels[]) {
        super(title);
        this.caller = caller;

        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());

        GridPanel gridPanel = new GridPanel();
        gridPanel.setBorder(new EmptyBorder(10, 10, 10, 20));
        String[] msgArray = CommonUtil.tokenize(message, "\n");
        for (int i = 0; i < msgArray.length; i++) {
            gridPanel.addComponent(null, "label" + i, new JLabel(msgArray[i]), 0, i, 1, 1);
        }

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setBorder(new EmptyBorder(10, 20, 10, 10));
        contentPane.add(iconLabel, BorderLayout.WEST);
        contentPane.add(gridPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 4));

        if (options == null || options.length == 0 || buttLabels == null || buttLabels.length != options.length) {
            options = new int[] {JOptionPane.OK_OPTION};
            buttLabels = new String[] {"OK"};
        }
        for (int i = 0; i < options.length; i++) {
            final int option = options[i];
            FixedSizeButton butt = new FixedSizeButton(buttLabels[i], 80, -1);
            butt.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    result = option;
                    setVisible(false);
                }
            });
            buttonPanel.add(butt);
        }

        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        pack();
    }

    protected void onOk() {

        setVisible(false);
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

    public int display() {
        setVisible(true);
        return result;
    }

    /**
     * Splits string in multiple lines if it is too long.
     * @param msg
     * @return Splitted string.
     */
    private String prepareMsg(String msg) {
        final int maxLength = 80;
        StringBuffer result = new StringBuffer("");
        int lineLength = 0;
        if (msg != null) {
            for (int i = 0; i < msg.length(); i++) {
                char ch = msg.charAt(i);
                if ( (ch == '\n') || (ch == ' ' && lineLength > maxLength) ) {
                    result.append('\n');
                    lineLength = 0;
                } else {
                    result.append(ch);
                    lineLength++;
                }
            }
        }

        return result.toString();
    }

}