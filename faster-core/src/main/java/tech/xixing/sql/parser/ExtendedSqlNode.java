package tech.xixing.sql.parser;

import tech.xixing.sql.parser.exception.SqlParseException;

/**
 * @author liuzhifei
 */
public interface ExtendedSqlNode {

    void validate() throws SqlParseException;
}
