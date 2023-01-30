package tech.xixing.sql.adapter;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.sql.ExtendedSqlRowTypeNameSpec;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author liuzhifei
 * @since 0.1
 */
public class JsonEnumerator implements Enumerator<Object[]> {

    private Enumerator<Object[]> enumerator;

    public JsonEnumerator(JSONArray jsonarr) {
        List<Object[]> objs = new ArrayList<Object[]>();
        for (Object obj : jsonarr) {
            objs.add(((JSONObject) obj).values().toArray());
        }
        enumerator = Linq4j.enumerator(objs);
    }

    public JsonEnumerator(JSONArray jsonarr, LinkedHashMap<String,Object> fields) {
        List<Object[]> objs = new ArrayList<Object[]>();

        for (Object obj : jsonarr) {
            JSONObject jsonObject = (JSONObject) obj;
            Object[] objects = new Object[fields.size()];
            int i = 0;
            for (String key : fields.keySet()) {
                objects[i] = jsonObject.get(key);
                if(objects[i] instanceof JSONObject&&fields.get(key) instanceof ExtendedSqlRowTypeNameSpec){
                    JSONObject data = (JSONObject) objects[i];
                    int size = data.size();
                    Object[] objects1 = new Object[size +1];
                    data.values().toArray(objects1);
                    // 将原始数据存到obj[length]
                    objects1[size] = data;
                    objects[i] =objects1;
                }
                // Row row = Row.of()
                // objects[i]= jsonObject.computeIfAbsent(key,k->"");
                i++;
            }
            objs.add(objects);
            //objs.add(((JSONObject) obj).values().toArray());
        }
        enumerator = Linq4j.enumerator(objs);
    }

    @Override
    public Object[] current() {
        return (Object[]) enumerator.current();
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
        enumerator.close();
    }
}
