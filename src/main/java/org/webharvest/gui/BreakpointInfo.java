package org.webharvest.gui;

/**
 * Information about single breakpoint in XML editor.
 */
public class BreakpointInfo {

    private int lineNumber = 0;

    public BreakpointInfo(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void updateForAmount(int amount) {
        lineNumber += amount;
    }

}