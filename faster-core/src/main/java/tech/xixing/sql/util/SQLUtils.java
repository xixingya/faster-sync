package tech.xixing.sql.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.ddl.SqlColumnDeclaration;
import org.apache.calcite.sql.dialect.CalciteSqlDialect;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.util.Pair;
import tech.xixing.sql.parser.ddl.SqlCreateTable;
import tech.xixing.sql.parser.ddl.SqlTableColumn;
import tech.xixing.sql.parser.extend.CreateSqlParserImpl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * @author liuzhifei
 * @since 0.1
 */
public class SQLUtils {

    public static LinkedHashMap<String,Object> getFieldsByJSONObject(String json){
        LinkedHashMap<String,Object> fields = new LinkedHashMap<>();
        JSONObject jsonObject = JSONObject.parseObject(json);
        Set<String> keySet = jsonObject.keySet();
        for (String key : keySet) {
            Object value = jsonObject.get(key);
            fields.put(key,value.getClass());
        }
        return fields;
    }


    /**
     * 通过json创建一个表模版，对复合结构需要自行确认
     * @param json json
     * @param tableName 表名
     * @return 建表语句
     */
    public static String transformSqlByJsonObj(String json,String tableName){
        JSONObject jsonObject = JSONObject.parseObject(json);
        StringBuilder sb = new StringBuilder();
        sb.append("create table ").append(tableName).append("(").append("\n");
        Set<String> keySet = jsonObject.keySet();
        for (String key : keySet) {
            Object value = jsonObject.get(key);
            Class<?> aClass = value.getClass();
            String type = aClass.getSimpleName();

            if(JSONArray.class.isAssignableFrom(aClass)){
                type="ARRAY<STRING>";
            }
            if(JSONObject.class.isAssignableFrom(aClass)){
                type="MAP<STRING,STRING>";
            }
            sb.append(key).append(" ").append(type).append(",").append("\n");
        }
        String substring = sb.substring(0, sb.length()-2);
        return substring+"\n)";
    }

    public static String changeSQL2StandardCalciteSQL(String sql) throws SqlParseException {
//        SqlParser.Config config = SqlParser.config().
//                withQuoting(Quoting.BACK_TICK)
//                .withQuotedCasing(Casing.UNCHANGED)
//                .withUnquotedCasing(Casing.UNCHANGED);
        // 使用mysql语法去解析sql
        SqlParser.Config config = SqlParser.config().withLex(Lex.MYSQL);

        SqlParser sqlParser = SqlParser.create(sql, config);
        SqlNode sqlNode = sqlParser.parseQuery();
        SqlKind kind = sqlNode.getKind();

        //通过sqlNode把mysql的语法，改成Calcite的语法
        return sqlNode.toSqlString(CalciteSqlDialect.DEFAULT).getSql();
    }

    public static Pair<String,LinkedHashMap<String,Object>> getTableConfigByCreateSql(String sql) throws SqlParseException {

        SqlParser.Config config = SqlParser.config().withLex(Lex.MYSQL).withParserFactory(CreateSqlParserImpl.FACTORY);
        SqlParser sqlParser = SqlParser.create(sql, config);

        SqlNode sqlNode = sqlParser.parseStmt();
        if(!(sqlNode instanceof SqlCreateTable)){
            return null;
        }
        SqlCreateTable sqlCreateTable = (SqlCreateTable)sqlNode;

        String tableName = sqlCreateTable.getTableName().toString();
        SqlNodeList columnList = sqlCreateTable.getColumnList();

        LinkedHashMap<String,Object> fields = new LinkedHashMap<>();
        for (SqlNode node : columnList) {
            SqlTableColumn sqlTableColumn = (SqlTableColumn)node;
            String name = sqlTableColumn.getName().toString();
            SqlTypeNameSpec typeNameSpec = sqlTableColumn.getType().getTypeNameSpec();
            fields.put(name,typeNameSpec);
        }
        return Pair.of(tableName,fields);
    }

    private static void handleCreate(SqlNode sqlNode){
        SqlCreate sqlCreate = (SqlCreate) sqlNode;
        List<SqlNode> operandList = sqlCreate.getOperandList();
        for (SqlNode node : operandList) {
            SqlKind kind = node.getKind();
            if(kind.equals(SqlKind.IDENTIFIER)){
                SqlIdentifier sqlIdentifier = (SqlIdentifier) node;
                System.out.println(sqlIdentifier.toString());
            }
            if(kind.equals(SqlKind.OTHER)){
                SqlNodeList sqlNodeList = (SqlNodeList)node;
                for (SqlNode temp : sqlNodeList) {
                    SqlColumnDeclaration sqlColumnDeclaration = (SqlColumnDeclaration) temp;
                    System.out.println(sqlColumnDeclaration.dataType.getTypeName().toString());
                }
            }
            // System.out.println(node.toSqlString(CalciteSqlDialect.DEFAULT).getSql());
        }
    }

    public static void main(String[] args) throws SqlParseException {
        getTableConfigByCreateSql("CREATE TABLE ods_kafka_student_scores (\n" +
                "  `name` ROW<course STRING,score INT>,\n" +
                "  `list` ARRAY<ROW<course STRING,score INT>>\n" +
                ")");
    }
}
