package tech.xixing.sql.constants;

/**
 * Constants used throughout the SQL processing framework
 * 
 * @author liuzhifei
 * @since 0.2
 */
public final class SqlConstants {
    
    private SqlConstants() {
        // Utility class - prevent instantiation
    }
    
    // Schema constants
    public static final String DEFAULT_SCHEMA = "default";
    public static final String CALCITE_JDBC_URL = "jdbc:calcite:";
    
    // Configuration properties
    public static final String CASE_SENSITIVE_PROPERTY = "caseSensitive";
    public static final String CASE_SENSITIVE_FALSE = "false";
    
    // Default column names
    public static final String DEFAULT_VALUE_COLUMN = "value";
    public static final String DEFAULT_LINE_COLUMN = "line";
    
    // Logging constants
    public static final int MAX_JSON_LOG_LENGTH = 100;
    public static final String JSON_TRUNCATED_SUFFIX = "...";
    
    // Error messages
    public static final String ERROR_NULL_JSON_ARRAY = "JSON array cannot be null";
    public static final String ERROR_NULL_FIELDS = "Fields cannot be null";
    public static final String ERROR_NULL_ROW_CONVERTER = "Row converter cannot be null";
    public static final String ERROR_NULL_SQL_CONFIG = "SQL config cannot be null";
    public static final String ERROR_EMPTY_JSON_ARRAY = "JSON array cannot be empty";
    public static final String ERROR_NULL_SQL = "SQL cannot be null";
    public static final String ERROR_NULL_TABLE_NAME = "Table name cannot be null";
    public static final String ERROR_NULL_CREATE_SQL = "Create SQL cannot be null";
    public static final String ERROR_NULL_EXECUTE_SQL = "Execute SQL cannot be null";
}
