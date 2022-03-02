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

import org.webharvest.gui.ResourceManager;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * @author: Vladimir Nikic
 * Date: Sep 18, 2007
 */
public class GCPanel extends JPanel {

    private static final Dimension PERCENT_LABEL_DIMENSION = new Dimension(80, 20);
    private static final Dimension GC_BUTTON_DIMENSION = new Dimension(20, 20);
    
    private PercentLabel percentLabel;

    private class MemoryCheckThread extends Thread {
        public void run() {
            while (true) {
                refresh();
            }
        }

        private synchronized void refresh() {
            percentLabel.setText( getUsageString() );
            percentLabel.repaint();
            try {
                sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class PercentLabel extends JLabel {
        public PercentLabel() {
            this.setBackground(Color.white);
            this.setText(getUsageString());
            this.setBorder(new LineBorder(Color.gray));
            new MemoryCheckThread().start();
        }


        public Dimension getPreferredSize() {
            return PERCENT_LABEL_DIMENSION;
        }

        public void paint(Graphics g) {
            Color color = g.getColor();
            g.setColor(Color.white);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(new Color(160, 176, 228));
            int width = (int) (getWidth() * getPercentOfUsedMemory());
            g.fillRect(0, 0, width, getHeight());
            g.setColor(color);
            super.paint(g);
        }
    }

    public GCPanel(LayoutManager layout) {
        super(layout);
        this.percentLabel = new PercentLabel();
        this.percentLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(this.percentLabel);
        JButton gcButton = new SmallButton(ResourceManager.SMALL_TRASHCAN_ICON) {
            public Dimension getPreferredSize() {
                return GC_BUTTON_DIMENSION;
            }
        };
        gcButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.gc();
            }
        });
        gcButton.setFocusable(false);
        gcButton.setToolTipText("Run garbage collector");
        add(gcButton);
    }

    private long getFreeMemory() {
        return Runtime.getRuntime().freeMemory() / (1024*1024);
    }

    private long getTotalMemory() {
        return Runtime.getRuntime().totalMemory() / (1024*1024);
    }

    private double getPercentOfUsedMemory() {
        long total = getTotalMemory();
        long used = total - getFreeMemory();
        return total > 0 ? ((double)used)/total : 1d;
    }

    private String getUsageString() {
        long total = getTotalMemory();
        long used = total - getFreeMemory();

        return used + "M of " + total + "M"; 
    }

}