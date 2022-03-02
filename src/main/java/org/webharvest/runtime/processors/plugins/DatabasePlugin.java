package org.webharvest.runtime.processors.plugins;

import org.webharvest.exception.*;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.*;

import java.sql.*;
import java.util.*;

/**
 * Support for database operations.
 */
public class DatabasePlugin extends WebHarvestPlugin {

    private class DbParamInfo {
        private Variable value;
        private String type;

        private DbParamInfo(Variable value, String type) {
            this.value = value;
            this.type = type;
        }
    }

    private List<DbParamInfo> dbParams = null;

    public String getName() {
        return "database";
    }

    public Variable executePlugin(Scraper scraper, ScraperContext context) {
        String jdbc = evaluateAttribute("jdbcclass", scraper);
        String connection = evaluateAttribute("connection", scraper);
        String username = evaluateAttribute("username", scraper);
        String password = evaluateAttribute("password", scraper);
        int maxRows = evaluateAttributeAsInteger("max", -1, scraper);
        boolean isAutoCommit = evaluateAttributeAsBoolean("autocommit", true, scraper);

        Connection conn = scraper.getConnection(jdbc, connection, username, password);
        Variable body = executeBody(scraper, context);
        String sql = body.toString();

        try {
            conn.setAutoCommit(isAutoCommit);
            PreparedStatement statement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            int index = 1;
            if (dbParams != null) {
                for (DbParamInfo paramInfo: dbParams) {
                    if ("int".equalsIgnoreCase(paramInfo.type)) {
                        try {
                            int intValue = Integer.parseInt(paramInfo.value.toString());
                            statement.setInt(index, intValue);
                        } catch (NumberFormatException e) {
                            throw new PluginException("Error in SQL statement - invalid integer!", e);
                        }
                    } else if ("long".equalsIgnoreCase(paramInfo.type)) {
                        try {
                            long longValue = Long.parseLong(paramInfo.value.toString());
                            statement.setLong(index, longValue);
                        } catch (NumberFormatException e) {
                            throw new PluginException("Error in SQL statement - invalid long!", e);
                        }
                    } else if ("double".equalsIgnoreCase(paramInfo.type)) {
                        try {
                            double doubleValue = Double.parseDouble(paramInfo.value.toString());
                            statement.setDouble(index, doubleValue);
                        } catch (NumberFormatException e) {
                            throw new PluginException("Error in SQL statement - invalid long!", e);
                        }
                    } else if ("binary".equalsIgnoreCase(paramInfo.type)) {
                        statement.setBytes(index, paramInfo.value.toBinary());
                    } else {
                        statement.setString(index, paramInfo.value.toString());
                    }
                    index++;
                }
            }
            
            statement.execute();
            ResultSet resultSet = statement.getResultSet();

            if (resultSet != null) {
                ResultSetMetaData metadata = resultSet.getMetaData();
                ListVariable queryResult = new ListVariable();
                int columnCount = metadata.getColumnCount();
                DbColumnDescription colDescs[] = new DbColumnDescription[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    String colName = metadata.getColumnLabel(i);
                    int colType = metadata.getColumnType(i);
                    colDescs[i - 1] = new DbColumnDescription(colName, colType);
                }

                int rowCount = 0;
                while ( resultSet.next() && (maxRows < 0 || rowCount < maxRows) ) {
                    Object rowData[] = new Object[columnCount];
                    for (int i = 0; i < columnCount; i++) {
                        switch (colDescs[i].getType()) {
                            case Types.FLOAT:
                                rowData[i] = resultSet.getFloat(i + 1); break;
                            case Types.DOUBLE:
                            case Types.DECIMAL:
                            case Types.NUMERIC:
                            case Types.REAL:
                                rowData[i] = resultSet.getDouble(i + 1); break;
                            case Types.SMALLINT:
                            case Types.INTEGER:
                            case Types.TINYINT:
                                rowData[i] = resultSet.getInt(i + 1); break;
                            case Types.BLOB:
                            case Types.BINARY:
                            case Types.VARBINARY:
                            case Types.LONGVARBINARY:
                                rowData[i] = resultSet.getBytes(i + 1); break;
                            default:
                                rowData[i] = resultSet.getString(i + 1); break;
                        }
                    }

                    queryResult.addVariable( new DbRowVariable(colDescs, rowData) );
                    rowCount++;
                }
                return queryResult;
            } else {
                return new EmptyVariable();
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
        

    }

    public String[] getValidAttributes() {
        return new String[] {
                "jdbcclass",
                "connection",
                "username",
                "password",
                "max",
                "autocommit"};
    }

    public String[] getRequiredAttributes() {
        return new String[] {"jdbcclass", "connection"};
    }

    public String[] getAttributeValueSuggestions(String attributeName) {
        if ("output".equalsIgnoreCase(attributeName)) {
            return new String[] {"text", "xml"};
        } else if ("autocommit".equalsIgnoreCase(attributeName)) {
            return new String[] {"true", "false"};
        }
        return null;
    }


    public Class[] getDependantProcessors() {
        return new Class[] {
            DbParamPlugin.class,
        };
    }

    void addDbParam(Variable value, String type) {
        if (dbParams == null) {
            dbParams = new ArrayList<DbParamInfo>();
        }
        dbParams.add(new DbParamInfo(value, type));
    }

}