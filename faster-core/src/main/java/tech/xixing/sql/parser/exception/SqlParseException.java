package tech.xixing.sql.parser.exception;

import org.apache.calcite.sql.parser.SqlParserPos;

/**
 * @author liuzhifei
 */
public class SqlParseException extends Exception {
    private SqlParserPos errorPos;
    private String message;

    public SqlParseException(SqlParserPos errorPos, String message) {
        this.errorPos = errorPos;
        this.message = message;
    }

    public SqlParseException(SqlParserPos errorPos, String message, Exception e) {
        super(e);
        this.errorPos = errorPos;
        this.message = message;
    }
}
