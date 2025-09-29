package tech.xixing.sql;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import tech.xixing.sql.config.SQLConfig;
import tech.xixing.sql.constants.SqlConstants;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * SQL Transformer for processing JSON data with SQL queries.
 * This class handles the transformation of JSON data through SQL operations.
 * 
 * Note: This class is not thread-safe due to the stateful nature of SQLConfig.
 * Use separate instances for concurrent operations.
 * 
 * @author liuzhifei
 * @since 2.0
 */
@Slf4j
public class SQLTransformer {

    private final SQLConfig sqlConfig;

    /**
     * Constructor for SQLTransformer
     * 
     * @param sqlConfig the SQL configuration to use
     * @throws IllegalArgumentException if sqlConfig is null
     */
    public SQLTransformer(SQLConfig sqlConfig) {
        this.sqlConfig = Objects.requireNonNull(sqlConfig, SqlConstants.ERROR_NULL_SQL_CONFIG);
    }

    /**
     * Transform JSON array data using the configured SQL query.
     * This method is synchronized to ensure thread safety when using the same instance.
     *
     * @param jsonArray JSON array as string to transform
     * @return List of JSONObject results
     * @throws SQLException if SQL execution fails
     * @throws IllegalArgumentException if jsonArray is null or empty
     */
    public synchronized List<JSONObject> transform(String jsonArray) throws SQLException {
        if (jsonArray == null || jsonArray.trim().isEmpty()) {
            throw new IllegalArgumentException(SqlConstants.ERROR_EMPTY_JSON_ARRAY);
        }
        
        try {
            return doTransform(jsonArray);
        } catch (Exception e) {
            String truncatedJson = jsonArray.length() > SqlConstants.MAX_JSON_LOG_LENGTH 
                ? jsonArray.substring(0, SqlConstants.MAX_JSON_LOG_LENGTH) + SqlConstants.JSON_TRUNCATED_SUFFIX 
                : jsonArray;
            log.error("Transform error for data: {}", truncatedJson, e);
            throw e;
        }
    }

    /**
     * Internal method to perform the actual transformation with proper resource management
     */
    private List<JSONObject> doTransform(String jsonArray) throws SQLException {
        sqlConfig.setData(jsonArray);
        List<JSONObject> results = new ArrayList<>();
        
        PreparedStatement statement = sqlConfig.getStatement();
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                JSONObject jsonObject = new JSONObject();
                int columnCount = resultSet.getMetaData().getColumnCount();
                
                for (int i = 1; i <= columnCount; i++) {
                    String columnLabel = resultSet.getMetaData().getColumnLabel(i);
                    Object value = resultSet.getObject(i);
                    jsonObject.put(columnLabel, value);
                }
                results.add(jsonObject);
            }
        }
        return results;
    }

    /**
     * Get the current SQL configuration
     * 
     * @return the SQL configuration
     */
    public SQLConfig getSqlConfig() {
        return sqlConfig;
    }
}
