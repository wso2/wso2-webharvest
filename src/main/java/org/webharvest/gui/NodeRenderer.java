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
package org.webharvest.gui;

import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.*;
import java.awt.*;

/**
 * Tree cell renderer
 */
public class NodeRenderer extends DefaultTreeCellRenderer {

    public static final Color RUNNING_BG_COLOR = new Color(126, 214, 131);
    public static final Color EXECUTED_COLOR = new Color(0, 128, 0);
    public static final Color EXCEPTION_COLOR = Color.white;
    public static final Color EXCEPTION_BG_COLOR = Color.red;
    
    private TreeNodeInfo executingNodeInfo;

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode defaultMutableTreeNode = (DefaultMutableTreeNode) value;
            Object userObject =  defaultMutableTreeNode.getUserObject();
            if (userObject instanceof TreeNodeInfo) {
                TreeNodeInfo treeNodeInfo = (TreeNodeInfo) userObject;

                renderer.setToolTipText(null);

                if ( treeNodeInfo.hasException() ) {
                    renderer.setForeground(EXCEPTION_COLOR);
                    renderer.setBackgroundNonSelectionColor(EXCEPTION_BG_COLOR);
                    String msg = treeNodeInfo.getException().getMessage();
                    renderer.setToolTipText(msg == null ? "Error" : msg);
                } else {
                    renderer.setBackgroundNonSelectionColor(executingNodeInfo == treeNodeInfo ? RUNNING_BG_COLOR : Color.white);
                    if (!sel && executingNodeInfo != treeNodeInfo) {
                        if (treeNodeInfo.getExecutionCount() > 0) {
                            renderer.setForeground(EXECUTED_COLOR);
                        }
                    }
                }

                Icon icon = treeNodeInfo.getIcon();
                if (icon != null) {
                    renderer.setIcon(icon);
                }
            }
        }

        return renderer;
    }

    public TreeNodeInfo getExecutingNodeInfo() {
        return executingNodeInfo;
    }

    public void setExecutingNodeInfo(TreeNodeInfo executingNodeInfo) {
        this.executingNodeInfo = executingNodeInfo;
    }

    public void markException(Exception e) {
        if (this.executingNodeInfo != null) {
            this.executingNodeInfo.setException(e);
        }
    }
    
}