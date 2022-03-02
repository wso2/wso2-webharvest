/*  Copyright (c) 2006-2007, Vladimir Nikic
    All rights reserved.

    Redistribution and use of this software in source and binary forms,
    with or without modification, are permitted provided that the following
    conditions are met:

    * Redistributions of source code must retain the above
      copyright notice, this list of conditions and the
      following disclaimer.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the
      following disclaimer in the documentation and/or other
      materials provided with the distribution.

    * The name of Web-Harvest may not be used to endorse or promote
      products derived from this software without specific prior
      written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.

    You can contact Vladimir Nikic by sending e-mail to
    nikic_vladimir@yahoo.com. Please include the word "Web-Harvest" in the
    subject line.
*/
package org.webharvest.gui.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

public class DropDownButton extends CommonButton implements ActionListener {

    private JPopupMenu popup;
    private ArrayList items = new ArrayList();

    private boolean isChangable;
    private int selectedItem = -1;

    // list of DropDownButtonListener instances
    private java.util.List listeners = new ArrayList();

    public DropDownButton() {
        this.popup = new WHPopupMenu();
        addActionListener(this);
        this.isChangable = true;
    }

    public DropDownButton(String text, Icon icon, boolean isChangable) {
        this();
        this.isChangable = isChangable;
        this.setText(text + "      ");
        if (icon != null) {
            this.setIcon(icon);
        }
    }

    public void addMenuSeparator() {
        this.popup.addSeparator();
    }

    public void addMenuItem(JMenuItem item) {
        this.popup.add(item);
        item.addActionListener(this);
        this.items.add(item);
    }

    public void paint(Graphics g) {
        super.paint(g);

        int w = getWidth();
        int h = getHeight();
        int xPoints[] = new int[] {w - 14, w - 7, w - 11};
        int yPoints[] = new int[] {h/2 - 2, h/2 - 2, h/2 + 2};

        g.setColor(Color.black);
        g.fillPolygon(xPoints, yPoints, 3);

    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == this) {
            this.popup.pack();
            this.popup.show(this, 0, this.getHeight());
        } else {
            int oldSelectedItem = selectedItem;

            // try to find selected item
            Iterator iterator = this.items.iterator();
            int index = 0;
            while (iterator.hasNext()) {
                Object curr =  iterator.next();
                if (curr == source) {
                    changeSelectedTo(index);
                    break;
                }
                index++;
            }

            this.popup.setVisible(false);

            if (!this.isChangable || selectedItem != oldSelectedItem) {
                // notify all listeners
                Iterator listenerIterator = this.listeners.iterator();
                while (listenerIterator.hasNext()) {
                    DropDownButtonListener curr =  (DropDownButtonListener) listenerIterator.next();
                    curr.onChange(this);
                }
            }
        }
    }

    public int getSelectedItem() {
        return selectedItem;
    }

    public void changeSelectedTo(int index) {
        if (index >= 0 && index < this.items.size()) {
            this.selectedItem = index;
            if (this.isChangable) {
                JMenuItem itemAtIndex = (JMenuItem) this.items.get(index);
                this.setText(itemAtIndex.getText() + "  ");
                this.setIcon(itemAtIndex.getIcon());
                this.invalidate();
            }
        }
    }

    public void addListener(DropDownButtonListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(DropDownButtonListener listener) {
        this.listeners.remove(listener);
    }

}