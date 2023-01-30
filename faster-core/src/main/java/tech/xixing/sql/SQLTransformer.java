package tech.xixing.sql;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.sql.ExtendedSqlRowTypeNameSpec;
import tech.xixing.sql.config.SQLConfig;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author liuzhifei
 * @since 2.0
 * SQL Transform when input a json and output a json
 */
@Slf4j
@Data
public class SQLTransformer {

    private SQLConfig sqlConfig;

    public SQLTransformer(SQLConfig sqlConfig){
        this.sqlConfig = sqlConfig;
    }


    public List<JSONObject> transform(String jsonArray) {
        sqlConfig.setData(jsonArray);
        List<JSONObject> res = new ArrayList<>();
        try{
            ResultSet resultSet = sqlConfig.getStatement().executeQuery();
            LinkedHashMap<String, Object> fields = sqlConfig.getFields();
            while (resultSet.next()) {
                JSONObject jo = new JSONObject();
                int n = resultSet.getMetaData().getColumnCount();
                for (int i = 1; i <= n; i++) {
                    String columnName = resultSet.getMetaData().getColumnName(i);
                    Object type = fields.get(columnName);
                    Object object = resultSet.getObject(i);
                    if(type instanceof Class){
                        Class<?> aClass = (Class<?>) type;
                        if(JSONObject.class.equals(aClass)&&object!=null){
                            object = JSONObject.parseObject(object.toString());
                        }
                        if(JSONArray.class.equals(aClass)&&object!=null){
                            object = JSONArray.parseArray(object.toString());
                        }
                    }else if(type instanceof ExtendedSqlRowTypeNameSpec){
                        object = resultSet.getObject(i);
                    }

                    jo.put(resultSet.getMetaData().getColumnLabel(i), object);
                }
                res.add(jo);
                // System.out.println(jo.toJSONString());
            }
        }catch (Exception e){
            log.error("exec query error sql = {}",sqlConfig.getSql(),e);
        }
        return res;
    }

}
