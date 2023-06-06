package tech.xixing.sql.udf;

import tech.xixing.sql.anno.Udf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liuzhifei
 * @since 0.1
 */
public class TestUdf {

    // @Udf(name = "test_split")
    public static String[] split(String str,String reg){
        if(str==null){
            return null;
        }
        return str.split(reg);
    }

    @Udf(name = "list_test")
    public static Object[] getListTest(){
        return new ArrayList<Object>(){{add("aaa");}}.toArray();
    }

    @Udf(name = "list_test2")
    public static List<Object> getList2(){
        return new ArrayList<>();
    }

    @Udf(name = "map_test")
    public static Map<String,Object> getMapTest(){
        return new HashMap<>();
    }
}
