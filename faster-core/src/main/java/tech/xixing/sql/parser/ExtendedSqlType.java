package tech.xixing.sql.parser;

import org.apache.calcite.sql.SqlDataTypeSpec;
import org.apache.calcite.sql.SqlWriter;

/**
 * @author liuzhifei
 */
public interface ExtendedSqlType {


    static void unparseType(SqlDataTypeSpec type,
                            SqlWriter writer,
                            int leftPrec,
                            int rightPrec) {
        if (type.getTypeName() instanceof ExtendedSqlType) {
            type.getTypeName().unparse(writer, leftPrec, rightPrec);
        } else {
            type.unparse(writer, leftPrec, rightPrec);
        }
    }
}
