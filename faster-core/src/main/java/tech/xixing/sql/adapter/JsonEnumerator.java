package tech.xixing.sql.adapter;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.sql.ExtendedSqlRowTypeNameSpec;
import org.apache.calcite.sql.SqlBasicTypeNameSpec;
import org.apache.calcite.sql.type.SqlTypeName;
import tech.xixing.sql.constants.SqlConstants;
import tech.xixing.sql.convert.RowConverter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * JSON Enumerator that converts JSON data into structured rows for SQL processing.
 * Supports type conversion and nested object handling.
 * 
 * @author liuzhifei
 * @since 0.1
 */
@Slf4j
public class JsonEnumerator implements Enumerator<Object[]> {

    private final Enumerator<Object[]> enumerator;

    /**
     * Constructor for simple JSON array enumeration without type conversion
     * 
     * @param jsonArray the JSON array to enumerate
     */
    public JsonEnumerator(JSONArray jsonArray) {
        if (jsonArray == null) {
            throw new IllegalArgumentException(SqlConstants.ERROR_NULL_JSON_ARRAY);
        }
        
        List<Object[]> rows = new ArrayList<>();
        try {
            for (Object obj : jsonArray) {
                if (obj instanceof JSONObject) {
                    JSONObject jsonObject = (JSONObject) obj;
                    rows.add(jsonObject.values().toArray());
                } else {
                    log.warn("Skipping non-JSONObject element: {}", obj);
                }
            }
        } catch (Exception e) {
            log.error("Error processing JSON array", e);
            throw new RuntimeException("Failed to process JSON array", e);
        }
        
        enumerator = Linq4j.enumerator(rows);
    }

    /**
     * Constructor for typed JSON enumeration with field definitions and row conversion
     * 
     * @param jsonArray the JSON array to enumerate
     * @param fields field definitions with types
     * @param rowConverter converter for type transformations
     */
    public JsonEnumerator(JSONArray jsonArray, LinkedHashMap<String, Object> fields, RowConverter rowConverter) {
        if (jsonArray == null) {
            throw new IllegalArgumentException(SqlConstants.ERROR_NULL_JSON_ARRAY);
        }
        if (fields == null) {
            throw new IllegalArgumentException(SqlConstants.ERROR_NULL_FIELDS);
        }
        if (rowConverter == null) {
            throw new IllegalArgumentException(SqlConstants.ERROR_NULL_ROW_CONVERTER);
        }
        
        List<Object[]> rows = new ArrayList<>();
        
        try {
            for (Object obj : jsonArray) {
                if (!(obj instanceof JSONObject)) {
                    log.warn("Skipping non-JSONObject element: {}", obj);
                    continue;
                }
                
                JSONObject jsonObject = (JSONObject) obj;
                Object[] rowData = processJsonObject(jsonObject, fields, rowConverter);
                rows.add(rowData);
            }
        } catch (Exception e) {
            log.error("Error processing typed JSON array", e);
            throw new RuntimeException("Failed to process typed JSON array", e);
        }
        
        enumerator = Linq4j.enumerator(rows);
    }

    /**
     * Process a single JSON object into a row array based on field definitions
     */
    private Object[] processJsonObject(JSONObject jsonObject, LinkedHashMap<String, Object> fields, RowConverter rowConverter) {
        Object[] rowData = new Object[fields.size()];
        int index = 0;
        
        for (String fieldName : fields.keySet()) {
            Object fieldType = fields.get(fieldName);
            Object fieldValue = jsonObject.get(fieldName);
            
            try {
                if (fieldType instanceof SqlBasicTypeNameSpec) {
                    // Handle basic SQL types with conversion
                    rowData[index] = convertBasicType(fieldValue, (SqlBasicTypeNameSpec) fieldType, rowConverter);
                } else if (fieldType instanceof ExtendedSqlRowTypeNameSpec) {
                    // Handle complex/nested types
                    rowData[index] = convertComplexType(fieldValue);
                } else {
                    // Direct assignment for other types
                    rowData[index] = fieldValue;
                }
            } catch (Exception e) {
                log.warn("Error converting field '{}' with type '{}' and value '{}': {}", 
                    fieldName, fieldType, fieldValue, e.getMessage());
                rowData[index] = fieldValue; // Fallback to original value
            }
            
            index++;
        }
        
        return rowData;
    }

    /**
     * Convert basic SQL type values using the row converter
     */
    private Object convertBasicType(Object value, SqlBasicTypeNameSpec typeSpec, RowConverter rowConverter) {
        String typeName = typeSpec.getTypeName().toString();
        SqlTypeName sqlTypeName = SqlTypeName.get(typeName);
        if (sqlTypeName != null) {
            return rowConverter.convert(sqlTypeName, value);
        }
        return value;
    }

    /**
     * Convert complex/nested JSON objects
     */
    private Object convertComplexType(Object value) {
        if (value instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) value;
            int size = jsonObject.size();
            Object[] resultArray = new Object[size + 1];
            
            // Copy values to array
            jsonObject.values().toArray(resultArray);
            
            // Store original JSONObject at the end for reference
            resultArray[size] = jsonObject;
            
            return resultArray;
        }
        return value;
    }

    @Override
    public Object[] current() {
        return enumerator.current();
    }

    @Override
    public boolean moveNext() {
        return enumerator.moveNext();
    }

    @Override
    public void reset() {
        enumerator.reset();
    }

    @Override
    public void close() {
        if (enumerator != null) {
            enumerator.close();
        }
    }
}
