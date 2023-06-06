package tech.xixing.sql.convert;

import org.apache.calcite.sql.type.SqlTypeName;

/**
 * @author liuzhifei
 * @since 0.2
 */
public interface RowConverter {

    /**
     * convert element to correct type
     */
    Object convert(SqlTypeName sqlTypeName, Object element);
}
