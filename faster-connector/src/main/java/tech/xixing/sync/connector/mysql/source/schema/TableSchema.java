package tech.xixing.sync.connector.mysql.source.schema;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liuzhifei
 * @since 1.0
 */
public class TableSchema {
    private String database;
    private String table;
    private String[] primaryKeys;
    private List<Column> columns = new ArrayList<>(16);

    private Integer tableId;

    public TableSchema(String database, String table) {
        this.database = database;
        this.table = table;
    }

    public String getDatabase() {
        return database;
    }

    public String getTable() {
        return table;
    }

    public String[] getPrimaryKeys() {
        return primaryKeys;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }

    public String getFullName() {
        return database + "." + table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public void setPrimaryKeys(String[] primaryKeys) {
        this.primaryKeys = primaryKeys;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void addColumn(Column column) {
        this.columns.add(column);
    }

}
