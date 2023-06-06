package tech.xixing.sql.config;

import com.alibaba.fastjson2.JSONObject;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.util.Pair;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import tech.xixing.sql.SQLTransformer;
import tech.xixing.sql.udf.UdfFactory;
import tech.xixing.sql.util.SQLUtils;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import com.alibaba.fastjson2.JSONArray;

/**
 * @author liuzhifei
 * @since 0.1
 */
class SQLConfigTest {

    @Test
    void test01() throws SqlParseException, SQLException {

        Pair<String,LinkedHashMap<String,Object>> pair = SQLUtils.getTableConfigByCreateSql("create table t1(nickname string,uid int,varTimestamp bigint,status int)");
        SQLConfig config = new SQLConfig("create table t1(nickname string,uid int,varTimestamp bigint,status int)","select uid||'_'||nickname as esId, varTimestamp as last_dis_conn_time,status as test_online from t1 where status = 1");
        SQLTransformer sqlTransformer = new SQLTransformer(config);
        List<JSONObject> jsonObjects = sqlTransformer.transform("[{\"uid\":12345,\"status\":1,\"varTimestamp\":\"1675141523785\",\"appId\":10,\"nickname\":\"qaq\"}\n]");
        System.out.println(jsonObjects);
    }

    @Test
    void stressTest() throws Exception{
        Pair<String,LinkedHashMap<String,Object>> pair = SQLUtils.getTableConfigByCreateSql("create table t1(nickname string,uid int,varTimestamp bigint,status int)");
        SQLConfig config = new SQLConfig("select uid||'_'||nickname as esId, varTimestamp as last_dis_conn_time,status as test_online from t1 where status = 1", pair.left, pair.right);
        SQLTransformer sqlTransformer = new SQLTransformer(config);
        double avgTime = 0;
        for (int i = 0; i < 10000; i++) {
            JSONArray jsonArray = new JSONArray();
            for (int j = 0; j < 1000; j++) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("uid",(int)(Math.random()*1000));
                jsonObject.put("status",(int)(Math.random()*2));
                jsonObject.put("nickname",java.util.UUID.randomUUID().toString());
                jsonObject.put("varTimestamp",System.currentTimeMillis());
                jsonArray.add(jsonObject);
            }
            long l = System.currentTimeMillis();
            List<JSONObject> jsonObjects = sqlTransformer.transform(jsonArray.toString());
            avgTime = avgTime+(System.currentTimeMillis()-l);
            //System.out.println(System.currentTimeMillis()-l);
        }
        System.out.println(avgTime/10000);
        List<JSONObject> jsonObjects = sqlTransformer.transform("[{\"uid\":12345,\"status\":1,\"varTimestamp\":1675141523785,\"appId\":10,\"nickname\":\"qaq\"}\n]");
        System.out.println(jsonObjects);
    }

    @Test
    void testList() throws SqlParseException, SQLException {

        Pair<String,LinkedHashMap<String,Object>> pair = SQLUtils.getTableConfigByCreateSql("create table t1(nickname string,uid int,varTimestamp bigint,status int)");
        SQLConfig config = new SQLConfig("select cast(list_test2() as any) as lst, list_test() as nocastlst, uid||'_'||nickname as esId, varTimestamp as last_dis_conn_time,status as test_online from t1 where status = 1", pair.left, pair.right);
        SQLTransformer sqlTransformer = new SQLTransformer(config);
        List<JSONObject> jsonObjects = sqlTransformer.transform("[{\"uid\":12345,\"status\":1,\"varTimestamp\":\"1675141523785\",\"appId\":10,\"nickname\":\"qaq\"}\n]");
        Object list = jsonObjects.get(0).get("nocastlst");
        System.out.println(jsonObjects.get(0));
    }

    @Test
    void testListError() throws Exception{
        Pair<String,LinkedHashMap<String,Object>> pair = SQLUtils.getTableConfigByCreateSql("create table t1(nickname string,uid int,varTimestamp bigint,status int)");
        SQLConfig config = new SQLConfig("select list_test2() as lst, uid||'_'||nickname as esId, varTimestamp as last_dis_conn_time,status as test_online from t1 where status = 1", pair.left, pair.right);
        SQLTransformer sqlTransformer = new SQLTransformer(config);
        List<JSONObject> jsonObjects = sqlTransformer.transform("[{\"uid\":12345,\"status\":1,\"varTimestamp\":\"1675141523785\",\"appId\":10,\"nickname\":\"qaq\"}\n]");
        // json序列化会报错。
        

        Object list = jsonObjects.get(0).get("lst");
        System.out.println(jsonObjects.get(0));
    }


    @Test
    void testMap() throws SqlParseException, SQLException {
        UdfFactory.init();

        Pair<String,LinkedHashMap<String,Object>> pair = SQLUtils.getTableConfigByCreateSql("create table t1(nickname string,uid int,varTimestamp bigint,status int)");
        SQLConfig config = new SQLConfig("select map_test() as lst, uid||'_'||nickname as esId, varTimestamp as last_dis_conn_time,status as test_online from t1 where status = 1", pair.left, pair.right);
        SQLTransformer sqlTransformer = new SQLTransformer(config);
        List<JSONObject> jsonObjects = sqlTransformer.transform("[{\"uid\":12345,\"status\":1,\"varTimestamp\":\"1675141523785\",\"appId\":10,\"nickname\":\"qaq\"}\n]");
        Object list = jsonObjects.get(0).get("lst");
        System.out.println(JSONObject.toJSONString(list));
    }

}