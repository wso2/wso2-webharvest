package org.webharvest.gui.component;

import org.webharvest.utils.*;

import javax.swing.event.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author: Vladimir Nikic
 * Date: Apr 23, 2007
 */
abstract public class EditableComboBox extends WHComboBox {

    private boolean addNewItemsOnTop = false;

    private boolean isUppercase = false;

    // tells how menu items are fixed and are not removing from the drop down
    private int fixedItemsSize = -1;

    // number of items limit for the drop down
    private int dropDownLimit = -1;

    private int length;
    private ActionListener[] actionListeners;

    private boolean isSelectedFromPopup = false;

    /**
     * Constructor that allows specifying wether new items are added to the top.
     */
    public EditableComboBox(int length, boolean addNewItemsOnTop) {
        this(length);
        this.addNewItemsOnTop = addNewItemsOnTop;
    }

    /**
     * Constructor.
     */
    public EditableComboBox(int length) {
        super();
        this.length = length;
        this.setEditable(true);
        final JTextField editField = (JTextField) this.getEditor().getEditorComponent();
        editField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if ( e.getKeyChar() == '\n' && !e.isControlDown() && !e.isAltDown() && !e.isShiftDown() ) {
                    Object value = getEditor().getItem();
                    if (isValidValue(value)) {
                        select(value);
                        onValue();
                    }
                } else {
                    if (isUppercase) {
                        e.setKeyChar(Character.toUpperCase(e.getKeyChar()));
                    }
                    super.keyTyped(e);
                }
            }
        });

        addPopupMenuListener(new PopupMenuListener() {
            private int selectedIndex = -1;
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                isSelectedFromPopup = true;
                selectedIndex = getSelectedIndex();
            }
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                if (isSelectedFromPopup && selectedIndex != getSelectedIndex()) {
                    onValue();
                }
            }
            public void popupMenuCanceled(PopupMenuEvent e) {
                isSelectedFromPopup = false;
            }
        });

        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if ( "comboBoxChanged".equals(e.getActionCommand()) ) {
                    if (!isPopupVisible()) {
                        Object value = getEditor().getItem();
                        if (isValueInList(value)) {
                            onValue();
                        }
                    } else {
                        isSelectedFromPopup = true;
                    }
                }
            }
        });

        editField.addFocusListener (new FocusListener () {
            public void focusGained (FocusEvent e)
            {
                ((JTextField)getEditor ().getEditorComponent ()).selectAll();
            }

            public void focusLost (FocusEvent e) {}
        });
    }

    private boolean isValueInList(Object value) {
        if (value != null) {
            int itemCount = getItemCount();
            for (int i = 0; i < itemCount; i++) {
                if ( value.equals(getItemAt(i)) ) {
                    return true;
                }
            }
        }
        return false;
    }

    public final void onValue() {
        Object value = getEditor().getItem();
        if (isValidValue(value)) {
            select(value);
            execute(value);
        }
    }

    protected boolean isValidValue(Object value) {
        return value != null;
    }

    abstract protected void execute(Object value);

    public void setDropDownLimit(int dropDownLimit) {
        this.dropDownLimit = dropDownLimit;
    }

    public void setFixedItemsSize(int fixedItemsSize) {
        this.fixedItemsSize = fixedItemsSize;
    }

    public Object getValue() {
        return getEditor().getItem();
    }

    public Dimension getPreferredSize () {
        Dimension size = super.getPreferredSize ();
        int charsWidth = getFontMetrics (getFont ()).charWidth ('H') * length;
        return new Dimension (charsWidth, size.height);
    }

    public void removeFromCombo(Object item) {
        if (item != null) {
            String itemAsString = item.toString();
            int itemCount = getItemCount();
            for ( int i = Math.max(0, fixedItemsSize); i < itemCount; i++ ) {
                Object currItem = getItemAt(i);
                if ( itemAsString.equals(currItem.toString()) ) {
                    ActionListener[] actionListeners = getActionListeners();

                    disableActionListeners();
                    removeItem(currItem);
                    enableActionListeners();

                    return;
                }
            }
        }
    }

    private boolean itemExists(Object item) {
        if (item != null) {
            String itemAsString = item.toString();
            int itemCount = getItemCount();
            for ( int i = 0; i < itemCount; i++ ) {
                Object currItem = getItemAt(i);
                if ( currItem != null && itemAsString.equals(currItem.toString()) ) {
                    return true;
                }
            }
        }

        return false;
    }

    public void addItem(Object item) {
        if (CommonUtil.isEmptyString(item)) {
            return;
        }

        removeFromCombo(item);

        if ( itemExists(item) ) {
            return;
        }

        disableActionListeners();

        if (this.addNewItemsOnTop) {
            super.insertItemAt(item, 0);
        } else {
            super.addItem(item);
        }

        int count = this.getItemCount();

        // if list is oversized
        if (this.dropDownLimit > 0 && count > this.dropDownLimit && count > this.fixedItemsSize) {
            if (this.addNewItemsOnTop) {
                this.removeItemAt(this.dropDownLimit - this.fixedItemsSize);
            } else {
                this.removeItemAt(this.fixedItemsSize);
            }
        }

        enableActionListeners();
    }

    public void select(Object item) {
        if ( item != null ) {
            addItem(item);
            String itemAsString = item.toString();
            int itemCount = getItemCount();
            for ( int i = 0; i < itemCount; i++ ) {
                Object currItem = getItemAt(i);
                if ( currItem != null && itemAsString.equals(currItem.toString()) ) {
                    disableActionListeners();
                    setSelectedIndex(i);
                    enableActionListeners();
                    break;
                }
            }
        }
    }

    private void disableActionListeners() {
        if (actionListeners == null) {
            actionListeners = getActionListeners();
        }
        for (int j = 0; j < actionListeners.length; j++) {
            removeActionListener( actionListeners[j] );
        }
    }

    private void enableActionListeners() {
        if (actionListeners != null) {
            for (int j = 0; j < actionListeners.length; j++) {
                addActionListener( actionListeners[j] );
            }
            actionListeners = null;
        }
    }

    public boolean isUppercase() {
        return isUppercase;
    }

    public void setUppercase(boolean uppercase) {
        isUppercase = uppercase;
    }

}