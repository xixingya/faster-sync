package tech.xixing.sql;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.sql.ExtendedSqlRowTypeNameSpec;
import tech.xixing.sql.config.SQLConfig;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author liuzhifei
 * @since 2.0
 * SQL Transform when input a json and output a json
 */
@Slf4j
@Data
public class SQLTransformer {

    private SQLConfig sqlConfig;

    public SQLTransformer(SQLConfig sqlConfig) {
        this.sqlConfig = sqlConfig;
    }


    /**
     * synchronized statement because is thread unsafe
     *
     * @param jsonArray
     * @return
     * @throws SQLException
     */
    public List<JSONObject> transform(String jsonArray) throws SQLException {
        ReentrantLock lock = new ReentrantLock();
        lock.lock();
        try {
            return doTransform(jsonArray);
        }
        catch (Exception e){
            log.error("transform error",e);
            throw e;
        }
        finally {
            lock.unlock();
        }
    }

    private List<JSONObject> doTransform(String jsonArray) throws SQLException {
        sqlConfig.setData(jsonArray);
        List<JSONObject> res = new ArrayList<>();
        PreparedStatement statement = sqlConfig.getStatement();
        ResultSet resultSet = statement.executeQuery();
        LinkedHashMap<String, Object> fields = sqlConfig.getFields();
        while (resultSet.next()) {
            JSONObject jo = new JSONObject();
            int n = resultSet.getMetaData().getColumnCount();
            for (int i = 1; i <= n; i++) {
                Object object = resultSet.getObject(i);
                jo.put(resultSet.getMetaData().getColumnLabel(i), object);
            }
            res.add(jo);
        }
        return res;
    }

}
