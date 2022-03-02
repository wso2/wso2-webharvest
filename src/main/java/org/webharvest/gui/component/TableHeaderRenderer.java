package org.webharvest.gui.component;

import org.webharvest.utils.CommonUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Renderer for table headers
 */
public class TableHeaderRenderer extends JLabel implements TableCellRenderer {

    protected int aligns[] = null;
    private boolean drawTopLine = true;
    private boolean drawBottomLine = true;

    public TableHeaderRenderer() {
        setOpaque(true);
        Border headerCellBorder = new Border() {
            public Insets getBorderInsets(Component c) {
                return new Insets(1, 2, 1, 2);
            }
            public boolean isBorderOpaque() {
                return false;
            }
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                g.setColor(Color.gray);
                if (drawTopLine) {
                    g.drawLine(0, 0, width - 1, 0);
                }
                g.drawLine(width - 1, 0, width - 1, height - 1);
                if (drawBottomLine) {
                    g.drawLine(0, height - 1, width - 1, height - 1);
                }
            }
        };
        setBorder(headerCellBorder);
    }

    public void setAligns(int[] aligns) {
        this.aligns = aligns;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        String text = CommonUtil.nvl(value, "");
        String[] tokens = CommonUtil.tokenize(text, "\n");
        String colName = tokens.length > 0 ? tokens[0] : " ";
        setText(colName);
        setToolTipText(tokens.length > 1 ? tokens[1] : colName);
        setBackground( new Color(200, 198, 188) );
        setForeground(  Color.black );

        if (aligns != null && column < aligns.length) {
            setHorizontalAlignment(aligns[column]);
        }
        return this;
    }

    public boolean isDrawTopLine() {
        return drawTopLine;
    }

    public void setDrawTopLine(boolean drawTopLine) {
        this.drawTopLine = drawTopLine;
    }

    public boolean isDrawBottomLine() {
        return drawBottomLine;
    }

    public void setDrawBottomLine(boolean drawBottomLine) {
        this.drawBottomLine = drawBottomLine;
    }

}