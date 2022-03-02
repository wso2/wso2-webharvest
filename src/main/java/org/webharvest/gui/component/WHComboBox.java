package org.webharvest.gui.component;

import org.webharvest.utils.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Combo boxes used in LiveCharts
 */
public class WHComboBox extends JComboBox {

    private class LCComboBoxUI extends BasicComboBoxUI {
        protected JButton createArrowButton() {
            JButton button = new JButton("") {
                public void paint(Graphics g) {
                    int w = getWidth();
                    int h = getHeight();

                    Graphics2D g2 = (Graphics2D) g;
                    GradientPaint gp = new GradientPaint(0, 0, new Color(255, 255, 255), 0, h - 1, new Color(215,211,204), false);
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, w, h);

                    g.setColor(WHComboBox.this.isEnabled() ? Color.black : Color.gray);
                    int wh = w / 2;
                    int hh = h / 2;
                    g.drawLine(wh - 3, hh - 2, wh + 3, hh - 2);
                    g.drawLine(wh - 2, hh - 1, wh + 2, hh - 1);
                    g.drawLine(wh - 1, hh, wh + 1, hh);
                    g.drawLine(wh, hh + 1, wh, hh + 1);
                }
            };
            button.setBorder(new EmptyBorder(0, 0, 0, 0));
            return button;
        }

        protected ListCellRenderer createRenderer() {
            return new ComboCellRenderer(super.createRenderer());
        }
    }

    private class ComboCellRenderer extends DefaultListCellRenderer {
        private ListCellRenderer defaultRenderer;

        private ComboCellRenderer(ListCellRenderer renderer) {
            this.defaultRenderer = renderer;

            setOpaque(true);
            setUI(new BasicLabelUI() {
                public void paint(Graphics g, JComponent c) {
                    int w = getWidth();
                    int h = getHeight();
                    Graphics2D g2 = (Graphics2D) g;
                    GradientPaint gp = new GradientPaint(0, 0, Color.white, 0, h - 1, new Color(215,211,204), false);
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, w, h);
                    setForeground(WHComboBox.this.isEnabled() ? Color.black : Color.gray);
                    super.paint(g, c);
                    if (WHComboBox.this.hasFocus()) {
                        g2.setColor(Color.black);
                        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] {1}, 1));
                        g2.drawRect(0, 0, w - 1, h - 1);
                    }
                }
            });
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            setText( CommonUtil.nvl(value, "") );
            if (index < 0) {
                return this;
            } else {
                Component comp = defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (!isSelected) {
                    comp.setBackground( WHComboBox.this.isEditable() ? Color.white : new Color(215,211,204) );
                }
                if (setCellFontBasedOnName && !CommonUtil.isEmptyString(value)) {
                    comp.setFont( new Font(value.toString(), Font.PLAIN, comp.getFont().getSize()) );
                }
                if (comp instanceof JLabel && expectIcons) {
                    JLabel label = (JLabel) comp;
                    if (value instanceof Iconifiable) {
                        Icon icon = ((Iconifiable)value).getIcon();
                        label.setIcon(icon);
                        if (icon != null) {
                            label.setIconTextGap(Math.max(4, 20 - icon.getIconWidth()));
                        }

                    } else {
                        label.setIcon(null);
                        label.setIconTextGap(4);
                    }
                }
                return comp;
            }
        }
    }

    private LCComboBoxUI comboBoxUI = new LCComboBoxUI();
    private boolean setCellFontBasedOnName = false;
    private boolean expectIcons = false;

    public WHComboBox() {
        defineLook();
    }

    public WHComboBox(ComboBoxModel aModel) {
        super(aModel);
        defineLook();
    }

    public WHComboBox(Object items[]) {
        super(items);
        defineLook();
    }

    public WHComboBox(Vector items) {
        super(items);
        defineLook();
    }

    private void defineLook() {
//        setUI(comboBoxUI);
//        setBorder( new WHControlsBorder() );
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (expectIcons) {
                    Object item = getSelectedItem();
                    if (item instanceof Iconifiable) {
                        ((ComboCellRenderer)getRenderer()).setIcon( ((Iconifiable)item).getIcon() );
                    }
                }
            }
        });
    }

    public Dimension getPreferredSize() {
        Dimension preferredSize = super.getPreferredSize();
//        return new Dimension(preferredSize.width, 16);
        return preferredSize;
    }

    public void addValues(Object [] values) {
        if (values != null) {
            ComboBoxModel model = getModel();
            if (model instanceof DefaultComboBoxModel) {
                DefaultComboBoxModel deafultModel = (DefaultComboBoxModel) model;
                for (int i = 0; i < values.length; i++) {
                    deafultModel.addElement(values[i]);
                }
            }
        }
    }

    public boolean isSetCellFontBasedOnName() {
        return setCellFontBasedOnName;
    }

    public void setSetCellFontBasedOnName(boolean setCellFontBasedOnName) {
        this.setCellFontBasedOnName = setCellFontBasedOnName;
    }

    public boolean isExpectIcons() {
        return expectIcons;
    }

    public void setExpectIcons(boolean expectIcons) {
        this.expectIcons = expectIcons;
    }

    public void setEnabled(boolean b) {
        super.setEnabled(b);
        ((ComboCellRenderer)getRenderer()).setEnabled(b);
    }

}