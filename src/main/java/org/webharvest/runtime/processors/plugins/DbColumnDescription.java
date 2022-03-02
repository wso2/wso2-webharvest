package org.webharvest.runtime.processors.plugins;

import org.webharvest.utils.CommonUtil;

/**
 * Information about database record columns.
 */
public class DbColumnDescription {

    private String name;
    private int type;
    private String identifier;

    public DbColumnDescription(String name, int type) {
        this.name = name;
        this.type = type;
        this.identifier = CommonUtil.getValidIdentifier(name);
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public String getIdentifier() {
        return identifier;
    }
}