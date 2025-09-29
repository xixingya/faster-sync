package tech.xixing.sql.adapter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.Statistic;
import org.apache.calcite.schema.Statistics;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.SqlTypeNameSpec;
import org.apache.calcite.util.Pair;
import tech.xixing.sql.convert.DefaultRowConverter;
import tech.xixing.sql.convert.RowConverter;
import tech.xixing.sql.constants.SqlConstants;
import tech.xixing.sql.parser.type.SqlTypeNameSpec2Type;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/**
 * JSON Table implementation that provides SQL access to JSON data.
 * Supports typed columns and custom row conversion.
 * 
 * @author liuzhifei
 * @since 0.1
 */
@Slf4j
public class JsonTable extends AbstractTable implements ScannableTable {

    private final JSONArray jsonArray;
    private final LinkedHashMap<String, Object> fields;
    private RowConverter rowConverter = new DefaultRowConverter();

    /**
     * Constructor for JSON table without explicit field definitions
     * 
     * @param jsonArray the JSON array data
     */
    public JsonTable(JSONArray jsonArray) {
        this.jsonArray = Objects.requireNonNull(jsonArray, SqlConstants.ERROR_NULL_JSON_ARRAY);
        this.fields = null;
        log.debug("Created JsonTable with {} rows (no field definitions)", jsonArray.size());
    }

    /**
     * Constructor for JSON table with explicit field definitions
     * 
     * @param jsonArray the JSON array data
     * @param fields field definitions with types
     */
    public JsonTable(JSONArray jsonArray, LinkedHashMap<String, Object> fields) {
        this.jsonArray = Objects.requireNonNull(jsonArray, SqlConstants.ERROR_NULL_JSON_ARRAY);
        this.fields = Objects.requireNonNull(fields, SqlConstants.ERROR_NULL_FIELDS);
        log.debug("Created JsonTable with {} rows and {} field definitions", 
            jsonArray.size(), fields.size());
    }

    @Override
    public RelDataType getRowType(RelDataTypeFactory typeFactory) {
        List<RelDataType> types = new ArrayList<>();
        List<String> names = new ArrayList<>();

        if (fields != null) {
            // Use predefined field definitions
            for (String fieldName : fields.keySet()) {
                names.add(fieldName);
                Object fieldDefinition = fields.get(fieldName);
                
                try {
                    if (fieldDefinition instanceof SqlTypeNameSpec) {
                        SqlTypeNameSpec sqlTypeNameSpec = (SqlTypeNameSpec) fieldDefinition;
                        RelDataType relDataType = SqlTypeNameSpec2Type.convert(sqlTypeNameSpec, typeFactory);
                        types.add(relDataType);
                    } else if (fieldDefinition instanceof Class) {
                        Class<?> clazz = (Class<?>) fieldDefinition;
                        // Handle JSON types as strings for SQL compatibility
                        if (JSON.class.isAssignableFrom(clazz)) {
                            clazz = String.class;
                        }
                        RelDataType relDataType = typeFactory.createJavaType(clazz);
                        types.add(relDataType);
                    } else {
                        // Fallback to string type
                        log.warn("Unknown field definition type for '{}': {}", fieldName, fieldDefinition);
                        types.add(typeFactory.createJavaType(String.class));
                    }
                } catch (Exception e) {
                    log.error("Error creating type for field '{}': {}", fieldName, e.getMessage());
                    types.add(typeFactory.createJavaType(String.class));
                }
            }
        } else {
            // Infer schema from first JSON object
            inferSchemaFromData(typeFactory, types, names);
        }

        if (names.isEmpty()) {
            log.warn("No fields defined for JsonTable, using default string column");
            names.add(SqlConstants.DEFAULT_VALUE_COLUMN);
            types.add(typeFactory.createJavaType(String.class));
        }

        return typeFactory.createStructType(Pair.zip(names, types));
    }

    /**
     * Infer schema from the first JSON object in the array
     */
    private void inferSchemaFromData(RelDataTypeFactory typeFactory, List<RelDataType> types, List<String> names) {
        if (jsonArray.isEmpty()) {
            return;
        }

        try {
            JSONObject firstObject = jsonArray.getJSONObject(0);
            for (String key : firstObject.keySet()) {
                names.add(key);
                Object value = firstObject.get(key);
                RelDataType dataType = inferTypeFromValue(value, typeFactory);
                types.add(dataType);
            }
            log.debug("Inferred schema with {} fields from JSON data", names.size());
        } catch (Exception e) {
            log.warn("Error inferring schema from JSON data: {}", e.getMessage());
        }
    }

    /**
     * Infer RelDataType from a JSON value
     */
    private RelDataType inferTypeFromValue(Object value, RelDataTypeFactory typeFactory) {
        if (value == null) {
            return typeFactory.createJavaType(String.class);
        }
        
        if (value instanceof JSON) {
            return typeFactory.createJavaType(String.class);
        } else if (value instanceof String) {
            return typeFactory.createJavaType(String.class);
        } else if (value instanceof Integer) {
            return typeFactory.createJavaType(Integer.class);
        } else if (value instanceof Long) {
            return typeFactory.createJavaType(Long.class);
        } else if (value instanceof Double || value instanceof Float) {
            return typeFactory.createJavaType(Double.class);
        } else if (value instanceof Boolean) {
            return typeFactory.createJavaType(Boolean.class);
        } else {
            return typeFactory.createJavaType(value.getClass());
        }
    }

    @Override
    public Statistic getStatistic() {
        return Statistics.of(jsonArray.size(), List.of());
    }

    @Override
    public Enumerable<Object[]> scan(DataContext root) {
        return new AbstractEnumerable<Object[]>() {
            @Override
            public Enumerator<Object[]> enumerator() {
                return new JsonEnumerator(jsonArray, fields, rowConverter);
            }
        };
    }

    /**
     * Set a custom row converter for type transformations
     * 
     * @param rowConverter the row converter to use
     */
    public void setRowConverter(RowConverter rowConverter) {
        this.rowConverter = Objects.requireNonNull(rowConverter, SqlConstants.ERROR_NULL_ROW_CONVERTER);
        log.debug("Row converter set to: {}", rowConverter.getClass().getSimpleName());
    }

    /**
     * Get the current row converter
     * 
     * @return the current row converter
     */
    public RowConverter getRowConverter() {
        return rowConverter;
    }

    /**
     * Get the JSON array data
     * 
     * @return the JSON array
     */
    public JSONArray getJsonArray() {
        return jsonArray;
    }

    /**
     * Get the field definitions
     * 
     * @return the field definitions, or null if not defined
     */
    public LinkedHashMap<String, Object> getFields() {
        return fields;
    }
}
