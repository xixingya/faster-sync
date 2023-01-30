package tech.xixing.sql.adapter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
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
import tech.xixing.sql.parser.type.SqlTypeNameSpec2Type;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author liuzhifei
 * @since 0.1
 */
public class JsonTable extends AbstractTable implements ScannableTable {

    private final JSONArray jsonarr;

    private final LinkedHashMap<String,Object> fields;

    // private final Enumerable<Object> enumerable;

    public JsonTable(JSONArray obj) {
        this.jsonarr = obj;
        this.fields = null;
    }
    public JsonTable(JSONArray obj,LinkedHashMap<String,Object> fields) {
        this.jsonarr = obj;
        this.fields = fields;
    }


    @Override
    public RelDataType getRowType(RelDataTypeFactory typeFactory) {
        final List<RelDataType> types = new ArrayList<RelDataType>();
        final List<String> names = new ArrayList<String>();

        // 通过传入的字段判断
        if(fields!=null){
            for (String key : fields.keySet()) {
                names.add(key);
                Object obj =  fields.get(key);
                if(obj instanceof SqlTypeNameSpec){
                    SqlTypeNameSpec sqlTypeNameSpec = (SqlTypeNameSpec) obj;
                    RelDataType relDataType = SqlTypeNameSpec2Type.convert(sqlTypeNameSpec, typeFactory);
                    types.add(relDataType);
                }else {
                    Class clazz = (Class) obj;
                    //如果是json类型，则传入string类型
                    if(JSON.class.isAssignableFrom(clazz)){
                        clazz = String.class;
                    }
                    types.add(typeFactory.createJavaType(clazz));
                }
            }
            return typeFactory.createStructType(Pair.zip(names, types));
        }
        //没传field的情况
        JSONObject jsonobj = jsonarr.getJSONObject(0);
        for (String key : jsonobj.keySet()) {
            final RelDataType type;
            Object value = jsonobj.get(key);
            Class clazz = null;
            if(value instanceof JSON){
                clazz = String.class;
            }else {
                clazz = value.getClass();
            }
            type = typeFactory.createJavaType(clazz);
            names.add(key);
            types.add(type);
        }
        if (names.isEmpty()) {
            names.add("line");
            types.add(typeFactory.createJavaType(String.class));
        }
        return typeFactory.createStructType(Pair.zip(names, types));
    }

    @Override
    public Statistic getStatistic() {
        return Statistics.UNKNOWN;
    }

    @Override
    public Enumerable<Object[]> scan(DataContext root) {
        return new AbstractEnumerable<Object[]>() {
            @Override
            public Enumerator<Object[]> enumerator() {
                return new JsonEnumerator(jsonarr,fields);
            }
        };
    }
}
