package tech.xixing.sql.udf;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.googlecode.aviator.AviatorEvaluator;
import tech.xixing.sql.anno.Udf;

import java.util.Map;

/**
 * @author liuzhifei
 * @since 0.1
 */
public class AviatorUdf {

    @Udf(name = "aviator_func")
    public static Object execute(String jsonStr,String expression){
        JSONObject jsonObject = JSON.parseObject(jsonStr);
        return AviatorEvaluator.execute(expression, jsonObject, true);
    }

    @Udf(name = "aviator_func")
    public static Object execute2(Map map, String expression){
        return AviatorEvaluator.execute(expression, map, true);
    }
}
