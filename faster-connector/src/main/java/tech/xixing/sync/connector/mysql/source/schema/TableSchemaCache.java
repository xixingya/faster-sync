package tech.xixing.sync.connector.mysql.source.schema;

import tech.xixing.sync.connector.mysql.source.DataSource;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * one datasource  one cache
 *
 * @author liuzhifei
 * @since 1.0
 */
public class TableSchemaCache {

    public static final String SCHEMA_SQL_ALL = "select *  from INFORMATION_SCHEMA.COLUMNS";

    public static final String SCHEMA_SQL_BY_ONE = "select *  from INFORMATION_SCHEMA.COLUMNS where TABLE_SCHEMA = ? and TABLE_NAME = ?";

    private DataSource dataSource;

    private PreparedStatement statement;


    private final Map<String, TableSchema> TABLE_SCHEMA_MAP = new ConcurrentHashMap<>(16);


    public TableSchemaCache(DataSource dataSource) throws SQLException {
        this.dataSource = dataSource;
        ResultSet resultSet = dataSource.getConnection().createStatement().executeQuery(SCHEMA_SQL_ALL);
        syncTableSchema(resultSet);
    }


    public TableSchema getTableSchema(String schema, String table) {
        return TABLE_SCHEMA_MAP.get(schema + "." + table);
    }

    public void syncTableSchema(String schema, String table, Integer tableId) {
        TableSchema tableSchema = TABLE_SCHEMA_MAP.get(schema + "." + table);
        if (tableSchema == null) {
            // @Todo get table schema from mysql
            return;
        }
        if (tableSchema.getTableId() == null) {
            tableSchema.setTableId(tableId);
            return;
        }
        if (tableSchema.getTableId().equals(tableId)) {
            // equal the table id ,no need to sync
            return;
        }

        // @Todo get table schema from mysql


    }

    private void syncTableSchema(ResultSet rs) throws SQLException {
        Map<String, TableSchema> temp = new HashMap<>(16);
        while (rs.next()) {
            String schema = rs.getString("TABLE_SCHEMA");
            String table = rs.getString("TABLE_NAME");
            String columnName = rs.getString("COLUMN_NAME");
            String type = rs.getString("DATA_TYPE");
            String position = rs.getString("ORDINAL_POSITION");
            String key = schema + "." + table;

            TableSchema tableSchema = temp.computeIfAbsent(key, k -> new TableSchema(schema, table));
            tableSchema.addColumn(new Column(columnName, Integer.valueOf(position), type));
        }
        // overwrite the old table schema
        TABLE_SCHEMA_MAP.putAll(temp);
    }


}
