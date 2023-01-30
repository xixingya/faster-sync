package tech.xixing.sql.parser.type;

import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.StructKind;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.type.SqlTypeName;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author liuzhifei
 * @since 0.1
 */
public class SqlTypeNameSpec2Type {
    private static RelDataTypeFactory staticFac = new JavaTypeFactoryImpl();

    public static RelDataType convert(SqlTypeNameSpec elementTypeName,RelDataTypeFactory typeFactory){

        RelDataType relDataType = null;
        if(elementTypeName instanceof ExtendedSqlCollectionTypeNameSpec){
            ExtendedSqlCollectionTypeNameSpec extendedSqlCollectionTypeNameSpec = (ExtendedSqlCollectionTypeNameSpec)elementTypeName;
            SqlTypeNameSpec name = extendedSqlCollectionTypeNameSpec.getElementTypeName();
            relDataType = typeFactory.createArrayType(convert(name, typeFactory), -1);
            // return typeFactory.createTypeWithNullability(arrayType,true);
        }else if(elementTypeName instanceof ExtendedSqlRowTypeNameSpec){
            ExtendedSqlRowTypeNameSpec extendedSqlRowTypeNameSpec = (ExtendedSqlRowTypeNameSpec)elementTypeName;
            List<SqlIdentifier> fieldNames = extendedSqlRowTypeNameSpec.getFieldNames();
            List<SqlDataTypeSpec> fieldTypes = extendedSqlRowTypeNameSpec.getFieldTypes();
            relDataType = typeFactory.createStructType(
                    StructKind.PEEK_FIELDS_NO_EXPAND,
                    fieldTypes.stream()
                            .map(dt -> convert(dt.getTypeNameSpec(), typeFactory))
                            .collect(Collectors.toList()),
                    fieldNames.stream()
                            .map(SqlIdentifier::toString)
                            .collect(Collectors.toList()));
            // return typeFactory.createTypeWithNullability(structType,true);;
        } else if(elementTypeName instanceof SqlBasicTypeNameSpec){
            SqlBasicTypeNameSpec sqlBasicTypeNameSpec = (SqlBasicTypeNameSpec) elementTypeName;
            String type = sqlBasicTypeNameSpec.getTypeName().toString();
            SqlTypeName sqlTypeName = SqlTypeName.get(type);
            relDataType =  typeFactory.createSqlType(sqlTypeName);
        } else if(elementTypeName instanceof SqlMapTypeNameSpec){
            SqlMapTypeNameSpec sqlMapTypeNameSpec = (SqlMapTypeNameSpec)elementTypeName;
            SqlDataTypeSpec keyType = sqlMapTypeNameSpec.getKeyType();
            SqlDataTypeSpec valType = sqlMapTypeNameSpec.getValType();
            relDataType = typeFactory.createMapType(
                    convert(keyType.getTypeNameSpec(),typeFactory),
                    convert(valType.getTypeNameSpec(),typeFactory));
        }
        if(relDataType==null){
            String type = elementTypeName.toString();
            SqlTypeName sqlTypeName = SqlTypeName.get(type);
            relDataType = typeFactory.createSqlType(sqlTypeName);
        }
        return typeFactory.createTypeWithNullability(relDataType,true);
    }



    public static RelDataType convert(SqlTypeNameSpec elementTypeName){
        return convert(elementTypeName,staticFac);
    }
}
