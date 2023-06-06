package tech.xixing.sql.convert;

import cn.hutool.core.date.format.FastDateFormat;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.sql.type.SqlTypeName;

import java.util.TimeZone;

/**
 * @author liuzhifei
 * @since 0.2
 * <p>change one row type to another type</p>
 */
public class DefaultRowConverter implements RowConverter{

    public static FastDateFormat fastDateFormat = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", TimeZone.getDefault());

    @Override
    public Object convert(SqlTypeName sqlTypeName, Object element) {
        if (element == null || sqlTypeName == null) {
            return element;
        }
        try {
            switch (sqlTypeName) {
                case BOOLEAN:
                    if (element instanceof Boolean) {
                        return element;
                    }
                    return Boolean.parseBoolean(element.toString());
                case INTEGER:
                    if (element instanceof Integer) {
                        return element;
                    }
                    return Integer.parseInt(element.toString());
                case BIGINT:
                    if (element instanceof Long) {
                        return element;
                    }
                    return Long.parseLong(element.toString());
                case FLOAT:
                    if (element instanceof Float) {
                        return element;
                    }
                    return Float.parseFloat(element.toString());
                case DOUBLE:
                    if (element instanceof Double) {
                        return element;
                    }
                    return Double.parseDouble(element.toString());
                case TIMESTAMP:
                    if (element instanceof Long) {
                        return element;
                    }
                    return fastDateFormat.parse(element.toString()).getTime();
                case VARCHAR:
                    return element.toString();
                default:
                    return element;
            }
        } catch (Exception e) {
            afterException(e);
        }
        return element;

    }

    protected void afterException(Exception e){
        // extend this to add your logic
    }
}
