package tech.xixing.sync.connector.mysql.source;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author liuzhifei
 * @since 1.0
 */
public class MysqlConverter {
    public static List<String> convertRow2Str(List<Serializable[]> rows){

        List<String> result = new ArrayList<>();
        for (Serializable[] row : rows) {
            for (Serializable serializable : row) {
                if (isPrimaryType(serializable)) {
                    result.add(serializable.toString());
                }
                if(serializable instanceof Date){

                }
            }
        }
        return result;
    }

    public static boolean isPrimaryType(Object obj){
        return obj instanceof Integer || obj instanceof Long || obj instanceof String|| obj instanceof Byte || obj instanceof Short || obj instanceof Float || obj instanceof Double || obj instanceof Boolean || obj instanceof Character;
    }
}
