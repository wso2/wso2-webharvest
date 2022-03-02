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

import org.webharvest.gui.component.*;

import javax.swing.*;
import java.awt.*;

/**
 * Class that ease work with GUI staff.
 * All methods are static in order to be used easier from everywhere. The only
 * requirement is that parent component have to be initialized before any use.  
 *
 * @author: Vladimir Nikic
 * Date: May 17, 2007
 */
public class GuiUtils {

    // parent component for all the dialogs
    private static Component parent;

    // default file chooser
    private final static JFileChooser fileChooser = new JFileChooser();

    public static synchronized void init(Component parent) {
        GuiUtils.parent = parent;
        fileChooser.setFileFilter( new XmlFileFilter() );
        fileChooser.setMultiSelectionEnabled(true);
    }

    /**
     * Displays dialog with specified error message.
     * @param msg
     */
    public static void showErrorMessage(String msg) {
        new AlertDialog(
                null,
                "Error",
                msg,
                ResourceManager.ERROR_ICON,
                new int[] {JOptionPane.OK_OPTION},
                new String[] {"OK"}
        ).display();
    }

    /**
     * Displays dialog with specified warning message.
     * @param msg
     */
    public static void showWarningMessage(String msg) {
        new AlertDialog(
                null,
                "Warning",
                msg,
                ResourceManager.WARNING_ICON,
                new int[] {JOptionPane.OK_OPTION},
                new String[] {"OK"}
        ).display();
    }

    /**
     * Displays dialog with specified information.
     * @param msg
     */
    public static void showInfoMessage(String msg) {
        new AlertDialog(
                null,
                "Information",
                msg,
                ResourceManager.INFO_ICON,
                new int[] {JOptionPane.OK_OPTION},
                new String[] {"OK"}
        ).display();
    }

    public static int showQuestionBox(String msg, String title, boolean hasCancelButton, Icon icon) {
        int options[] = hasCancelButton ?
                new int[] {JOptionPane.YES_OPTION, JOptionPane.NO_OPTION, JOptionPane.CANCEL_OPTION} :
                new int[] {JOptionPane.YES_OPTION, JOptionPane.NO_OPTION};
        String buttLabels[] = hasCancelButton ? new String[] {"Yes", "No", "Cancel"} : new String[] {"Yes", "No"};
        return new AlertDialog(
                null,
                title,
                msg,
                icon,
                options,
                buttLabels
        ).display();
    }

    public static int showQuestionBox(String msg, boolean hasCancelButton) {
        return showQuestionBox(msg, "Question", hasCancelButton, ResourceManager.QUESTION_ICON);
    }

    public static int showWarningQuestionBox(String msg, boolean hasCancelButton) {
        return showQuestionBox(msg, "Warning", hasCancelButton, ResourceManager.QUESTION_ICON);
    }

    /**
     * @return Default file chooser instance for the application.
     */
    public static JFileChooser getFileChooser() {
        return fileChooser;
    }

    public static Frame getActiveFrame() {
        Window window = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        return window instanceof Frame ? (Frame)window : null;
    }
    
    public static void centerRelativeTo(Component comp, Component parent) {
        if ( comp != null && parent != null ) {
            Dimension dialogSize = comp.getSize();
            if (parent.isVisible()) {
                Point point = parent.getLocationOnScreen();
                Dimension parentSize = parent.getSize();
                int newX = point.x + (parentSize.width - dialogSize.width) / 2;
                int newY = point.y + (parentSize.height - dialogSize.height) / 2;
                if (newX >= 0 && newY >= 0) {
                    comp.setLocation(newX, newY);
                    return;
                }
            }
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int newX = (screenSize.width - dialogSize.width) / 2;
            int newY = (screenSize.height - dialogSize.height) / 2;
            if (newX >= 0 && newY >= 0) {
                comp.setLocation(newX, newY);
            }
        }
    }

    /**
     * Calculates position within the top parent of given component
     * @param component
     * @param parentClass
     * @return First parent of the specified component of specified class
     */
    public static Component getParentOfType(Component component, Class parentClass) {
        Component parent = component.getParent();
        while (parent != null) {
            if (parent.getClass() == parentClass) {
                return parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

}