package tech.xixing.sql.udf;

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
}
