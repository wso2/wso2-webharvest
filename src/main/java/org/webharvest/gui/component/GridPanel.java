package org.webharvest.gui.component;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Panel with GridBagLayout and handy methods for creation of components.
 */
public class GridPanel extends JPanel {

    private Map subpanels = new HashMap();
    private GridBagConstraints con;
    private GridBagConstraints incon;

    public GridPanel() {
        setLayout( new GridBagLayout() );
        this.con = new GridBagConstraints();
        this.con.fill = GridBagConstraints.BOTH;
        this.con.insets = new Insets(0, 0, 0, 0);
        this.con.anchor = GridBagConstraints.FIRST_LINE_START;
        this.con.weightx = 1.0;
        this.con.weighty = 1.0;

        this.incon = new GridBagConstraints();
        this.incon.fill = GridBagConstraints.HORIZONTAL;
        this.incon.insets = new Insets(2, 3, 2, 3);
        this.incon.anchor = GridBagConstraints.LINE_START;
        this.incon.weightx = 1.0;
        this.incon.weighty = 1.0;
    }

    public void addSubpanel(String id, String title, int x, int y, int width, int height) {
        JPanel newSubpanel = new JPanel(new GridBagLayout());
        newSubpanel.setBorder( title == null ? null : new WHTitledBorder(title) );
        subpanels.put(id, newSubpanel);

        con.gridx = x;
        con.gridy = y;
        con.gridwidth = width;
        con.gridheight = height;
        add(newSubpanel, con);
    }

    public void addComponents(String subpanelId, Component comps[], int x, int y, int width, int height, int align) {
        JPanel panel = new JPanel(new FlowLayout(align, 2, 0));
        panel.setOpaque(false);
        for (int i = 0; i < comps.length; i++) {
            panel.add(comps[i]);
        }

        Container container = null;
        if (subpanelId != null) {
            container = (Container) subpanels.get(subpanelId);
        }
        if (container == null) {
            container = this;
        }

        incon.gridx = x;
        incon.gridy = y;
        incon.gridwidth = width;
        incon.gridheight = height;
        container.add(panel, incon);
    }

    public void addComponent(String subpanelId, String id, Component comp, int x, int y, int width, int height) {
        Container container = null;
        if (subpanelId != null) {
            container = (Container) subpanels.get(subpanelId);
        }
        if (container == null) {
            container = this;
        }

        incon.gridx = x;
        incon.gridy = y;
        incon.gridwidth = width;
        incon.gridheight = height;
        container.add(comp, incon);
    }

    public GridBagConstraints getOuterConstants() {
        return con;
    }

    public GridBagConstraints getInnerConstants() {
        return incon;
    }

}
