package org.webharvest.gui;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Collection of breakpoints sorted by line number.
 */
public class BreakpointCollection extends ArrayList {

    public BreakpointCollection() {
    }

    /**
     * Adds new breakpoint (if none exists on the same line), keeping list
     * sorted by line numbers of the breakpoints.
     * @param breakpointInfo Breakpoint to be inserted
     * @return Position where breakpoint is inserted, or -1 if there was no insert
     */
    public int addBreakpoint(BreakpointInfo breakpointInfo) {
        if (breakpointInfo != null) {
            int lineNumber = breakpointInfo.getLineNumber();
            final int size = this.size();
            for (int i = 0; i < size; i++) {
                BreakpointInfo breakpoint = (BreakpointInfo) this.get(i);
                int currLineNumber = breakpoint.getLineNumber();
                if (currLineNumber == lineNumber) {
                    return -1;
                } else if (currLineNumber > lineNumber) {
                    add(i, breakpointInfo);
                    return i;
                }
            }
            add(breakpointInfo);
            return size;
        }

        return -1;
    }

    /**
     * Removes breakpoint on specified line number, if such exists.
     * @param lineNumber Line number of the breakpoint to be removed.
     */
    public void removeBreakpoint(int lineNumber) {
        final int size = this.size();
        for (int i = 0; i < size; i++) {
            BreakpointInfo breakpoint = (BreakpointInfo) this.get(i);
            int currLineNumber = breakpoint.getLineNumber();
            if (currLineNumber == lineNumber) {
                remove(i);
                return;
            } else if (currLineNumber > lineNumber) {
                return;
            }
        }
    }

    /**
     * Removes breakpoints in specified range of lines.
     * @param from Range start
     * @param to Range end
     */
    public void removeBreakpointsInRange(int from, int to) {
        Iterator iterator = iterator();
        while (iterator.hasNext()) {
            BreakpointInfo breakpoint = (BreakpointInfo) iterator.next();
            int currLineNumber = breakpoint.getLineNumber();
            if (currLineNumber >= from && currLineNumber <= to) {
                iterator.remove();
            } else if (currLineNumber > to) {
                return;
            }
        }
    }

    /**
     * Updates breakpoints from specified line for specified amount.
     * @param from Range start
     */
    public void updateBreakpoints(int from, int amount) {
        final int size = this.size();
        for (int i = 0; i < size; i++) {
            BreakpointInfo breakpoint = (BreakpointInfo) this.get(i);
            int currLineNumber = breakpoint.getLineNumber();
            if (currLineNumber >= from) {
                breakpoint.updateForAmount(amount);
            }
        }
    }

    /**
     * Checks if breakpoint at specified line number already exists.
     * @param lineNumber Line number for which breakpoint is looked for
     * @return True if breakpoint exists, false otherwise
     */
    public boolean isThereBreakpoint(int lineNumber) {
        final int size = this.size();
        for (int i = 0; i < size; i++) {
            BreakpointInfo breakpoint = (BreakpointInfo) this.get(i);
            int currLineNumber = breakpoint.getLineNumber();
            if (currLineNumber == lineNumber) {
                return true;
            } else if (currLineNumber > lineNumber) {
                return false;
            }
        }

        return false;
    }

}