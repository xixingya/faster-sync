package tech.xixing.sql.adapter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.google.common.collect.ImmutableMap;
import org.apache.calcite.linq4j.tree.Expression;
import org.apache.calcite.linq4j.tree.Expressions;
import org.apache.calcite.linq4j.tree.Types;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Schemas;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.calcite.util.BuiltInMethod;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of {@link org.apache.calcite.schema.Schema} that exposes the
 * public fields and methods in a json.
 */

/**
 * Implementation of {@link Schema} that exposes the
 * public fields and methods in a json.
 */
public class JsonSchema extends AbstractSchema {
    private volatile String target;
    private String databaseName;
    private JSONArray targetArray;

    // 初始化的时候保存的格式，如果没保存，则存在后续不一致问题。
    private LinkedHashMap<String, Object> fields;

    Map<String, Table> table = null;

    /**
     * Creates a JsonSchema.
     *
     * @param target Object whose fields will be sub-objects of the schema
     */
    public JsonSchema(String databaseName, String target) {
        super();
        this.databaseName = databaseName;
        if (!target.startsWith("[")) {
            this.target = '[' + target + ']';
        } else {
            this.target = target;
        }
        final ImmutableMap.Builder<String, Table> builder = ImmutableMap.builder();

        final Table table = fieldRelation();
        if (table != null) {
            builder.put(databaseName, table);
            this.table = builder.build();
        }
    }


    public JsonSchema(String topic, String target, LinkedHashMap<String,Object> fields) {
        super();
        this.databaseName = topic;
        if (!target.startsWith("[")) {
            this.target = '[' + target + ']';
        } else {
            this.target = target;
        }
        this.fields = fields;
        final ImmutableMap.Builder<String, Table> builder = ImmutableMap.builder();

        final Table table = fieldRelation();
        if (table != null) {
            builder.put(topic, table);
            this.table = builder.build();
        }
    }

    public void setTarget(String target) {
        this.target = target;
        final ImmutableMap.Builder<String, Table> builder = ImmutableMap.builder();

        final Table table = fieldRelation();
        if (table != null) {
            builder.put(databaseName, table);
            this.table = builder.build();
        }
    }

    public JsonSchema(String databaseName, JSONArray targetArray) {
        super();
        this.targetArray = targetArray;
        this.databaseName = databaseName;
        final ImmutableMap.Builder<String, Table> builder = ImmutableMap.builder();
        final Table table = fieldRelation();
        builder.put(databaseName, table);
        this.table = builder.build();

    }

    @Override
    public String toString() {
        return "JsonSchema(topic=" + databaseName + ":target=" + target + ")";
    }

    /**
     * Returns the wrapped object.
     *
     * <p>
     * May not appear to be used, but is used in generated code via
     * {@link BuiltInMethod#REFLECTIVE_SCHEMA_GET_TARGET}
     * .
     */
    public String getTarget() {
        return target;
    }

    @Override
    protected Map<String, Table> getTableMap() {
        return table;
    }

    /**
     * Returns an expression for the object wrapped by this schema (not the
     * schema itself).
     */
    Expression getTargetExpression(SchemaPlus parentSchema, String name) {
        return Types.castIfNecessary(target.getClass(),
                Expressions.call(Schemas.unwrap(getExpression(parentSchema, name), JsonSchema.class),
                        BuiltInMethod.REFLECTIVE_SCHEMA_GET_TARGET.method));
    }

    private <T> Table fieldRelation() {
        if (targetArray != null) {
            return new JsonTable(targetArray);
        }
        JSONArray jsonarr = JSON.parseArray(target);
        // final Enumerator<Object> enumerator = Linq4j.enumerator(list);
        if(fields!=null){
            return new JsonTable(jsonarr,fields);
        }
        return new JsonTable(jsonarr);
    }
}
