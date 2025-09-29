package tech.xixing.sql.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.ScalarFunctionImpl;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.util.Pair;
import tech.xixing.sql.adapter.JsonSchema;
import tech.xixing.sql.constants.SqlConstants;
import tech.xixing.sql.convert.RowConverter;
import tech.xixing.sql.udf.UdfFactory;
import tech.xixing.sql.util.SQLUtils;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

/**
 * SQL Configuration class that manages Calcite connections and schema setup.
 * This class is responsible for initializing the SQL execution environment.
 * 
 * @author liuzhifei
 * @since 0.1
 */
@Slf4j
@Getter
public class SQLConfig implements AutoCloseable {
    
    private final String sql;
    private final String tableName;
    private final LinkedHashMap<String, Object> fields;
    
    private JsonSchema jsonSchema;
    private SchemaPlus rootSchema;
    private PreparedStatement statement;
    private Connection connection;
    
    /**
     * Constructor for creating SQLConfig with explicit parameters
     * 
     * @param sql the SQL query to execute
     * @param tableName the table name to use
     * @param fields the field definitions
     * @throws SQLException if database initialization fails
     * @throws SqlParseException if SQL parsing fails
     */
    public SQLConfig(String sql, String tableName, LinkedHashMap<String, Object> fields) throws SQLException, SqlParseException {
        this.sql = Objects.requireNonNull(SQLUtils.changeSQL2StandardCalciteSQL(sql), SqlConstants.ERROR_NULL_SQL);
        this.tableName = Objects.requireNonNull(tableName, SqlConstants.ERROR_NULL_TABLE_NAME);
        this.fields = Objects.requireNonNull(fields, SqlConstants.ERROR_NULL_FIELDS);
        
        initializeCalcite();
    }

    /**
     * Constructor for creating SQLConfig from CREATE and SELECT statements
     * 
     * @param createSql the CREATE TABLE statement
     * @param executeSql the SELECT statement to execute
     * @throws SqlParseException if SQL parsing fails
     * @throws SQLException if database initialization fails
     */
    public SQLConfig(String createSql, String executeSql) throws SqlParseException, SQLException {
        Objects.requireNonNull(createSql, SqlConstants.ERROR_NULL_CREATE_SQL);
        Objects.requireNonNull(executeSql, SqlConstants.ERROR_NULL_EXECUTE_SQL);
        
        Pair<String, LinkedHashMap<String, Object>> tableConfig = SQLUtils.getTableConfigByCreateSql(createSql);
        this.sql = SQLUtils.changeSQL2StandardCalciteSQL(executeSql);
        this.fields = tableConfig.right;
        this.tableName = tableConfig.left;
        
        initializeCalcite();
    }

    /**
     * Initialize Calcite connection and schema
     */
    private void initializeCalcite() throws SQLException {
        Properties properties = new Properties();
        properties.setProperty(SqlConstants.CASE_SENSITIVE_PROPERTY, SqlConstants.CASE_SENSITIVE_FALSE);
        
        try {
            connection = DriverManager.getConnection(SqlConstants.CALCITE_JDBC_URL, properties);
            CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
            rootSchema = calciteConnection.getRootSchema();
            
            // Initialize JSON schema
            jsonSchema = new JsonSchema(tableName, "", fields);
            rootSchema.add(SqlConstants.DEFAULT_SCHEMA, jsonSchema);
            connection.setSchema(SqlConstants.DEFAULT_SCHEMA);
            
            // Register UDFs
            registerUserDefinedFunctions();
            
            // Prepare the statement
            statement = connection.prepareStatement(this.sql);
            
            log.debug("SQLConfig initialized successfully for table: {}", tableName);
        } catch (SQLException e) {
            log.error("Failed to initialize SQLConfig", e);
            close(); // Clean up resources on failure
            throw e;
        }
    }

    /**
     * Register user-defined functions for the table
     */
    private void registerUserDefinedFunctions() {
        try {
            Set<UdfConfig> udfConfigs = UdfFactory.getUdfByTable(tableName);
            for (UdfConfig udfConfig : udfConfigs) {
                rootSchema.add(udfConfig.getName(), ScalarFunctionImpl.create(udfConfig.getMethod()));
                log.debug("Registered UDF: {} for table: {}", udfConfig.getName(), tableName);
            }
        } catch (Exception e) {
            log.warn("Failed to register some UDFs for table: {}", tableName, e);
        }
    }

    /**
     * Set the JSON data for processing
     * 
     * @param jsonArray the JSON array as string
     */
    public void setData(String jsonArray) {
        Objects.requireNonNull(jsonArray, SqlConstants.ERROR_NULL_JSON_ARRAY);
        if (jsonSchema != null) {
            jsonSchema.setTarget(jsonArray);
        }
    }

    /**
     * Re-prepare the statement (useful if the SQL needs to be re-executed)
     * 
     * @throws SQLException if statement preparation fails
     */
    public void rePrepared() throws SQLException {
        if (statement != null) {
            statement.close();
        }
        if (connection != null && !connection.isClosed()) {
            statement = connection.prepareStatement(sql);
        }
    }

    /**
     * Set a custom row converter (placeholder for future implementation)
     * 
     * @param rowConverter the row converter to use
     */
    public void setRowConverter(RowConverter rowConverter) {
        // TODO: Implement row converter integration
        log.debug("Row converter set: {}", rowConverter);
    }

    /**
     * Close all resources associated with this configuration
     */
    @Override
    public void close() {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                log.warn("Error closing statement", e);
            }
        }
        
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                log.warn("Error closing connection", e);
            }
        }
        
        log.debug("SQLConfig resources closed");
    }
}
