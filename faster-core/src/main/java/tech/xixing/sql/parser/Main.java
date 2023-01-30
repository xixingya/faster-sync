package tech.xixing.sql.parser;

import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.sql.SqlDataTypeSpec;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlTypeNameSpec;
import org.apache.calcite.sql.type.SqlTypeUtil;
import org.apache.calcite.sql.validate.SqlValidatorUtil;
import tech.xixing.sql.parser.ddl.SqlCreateTable;
import tech.xixing.sql.parser.ddl.SqlTableColumn;
import tech.xixing.sql.parser.extend.CreateSqlParserImpl;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import tech.xixing.sql.parser.type.ExtendedSqlCollectionTypeNameSpec;
import tech.xixing.sql.parser.type.SqlTypeNameSpec2Type;

/**
 * @author liuzhifei
 */
public class Main {

    public static void main(String[] args) throws SqlParseException {
        String sql = "create table aaa(a ARRAY<ROW<course int,chat string>>)";

        SqlParser.Config config = SqlParser.config().withParserFactory(CreateSqlParserImpl.FACTORY).withLex(Lex.MYSQL);
        SqlParser sqlParser = SqlParser.create(sql, config);
        SqlNode sqlNode = sqlParser.parseQuery();

        RelDataTypeFactory typeFactory = new JavaTypeFactoryImpl();

        SqlCreateTable sqlCreateTable = (SqlCreateTable) sqlNode;
        SqlNode node = sqlCreateTable.getColumnList().get(0);
        SqlTableColumn sqlTableColumn = (SqlTableColumn)node;
        SqlDataTypeSpec type = sqlTableColumn.getType();

        SqlTypeNameSpec typeNameSpec = type.getTypeNameSpec();
        RelDataType relDataType = SqlTypeNameSpec2Type.convert(typeNameSpec);


        System.out.println(sqlNode);
    }
}
