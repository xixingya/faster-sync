package tech.xixing.sql.parser.ddl;

import static java.util.Objects.requireNonNull;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.util.ImmutableNullableList;

import java.util.List;

/**
 * @author liuzhifei
 */

public class SqlTableColumn extends SqlCall {
    private static final SqlSpecialOperator OPERATOR =
            new SqlSpecialOperator("COLUMN_DECL", SqlKind.COLUMN_DECL);

    private SqlIdentifier name;
    private SqlDataTypeSpec type;
    private SqlIdentifier alias;
    private SqlCharStringLiteral comment;

    public SqlTableColumn(SqlIdentifier name,
                          SqlDataTypeSpec type,
                          SqlIdentifier alias,
                          SqlCharStringLiteral comment,
                          SqlParserPos pos) {
        super(pos);
        this.name = requireNonNull(name, "Column name should not be null");
        this.type = requireNonNull(type, "Column type should not be null");
        this.alias = alias;
        this.comment = comment;
    }

    public SqlTableColumn(SqlIdentifier name,
                          SqlDataTypeSpec type,
                          SqlCharStringLiteral comment,
                          SqlParserPos pos) {
        super(pos);
        this.name = requireNonNull(name, "Column name should not be null");
        this.type = requireNonNull(type, "Column type should not be null");
        this.alias = null;
        this.comment = comment;
    }

    @Override
    public SqlOperator getOperator() {
        return OPERATOR;
    }

    @Override
    public List<SqlNode> getOperandList() {
        return ImmutableNullableList.of(name, type, comment);
    }

    @Override
    public void unparse(SqlWriter writer, int leftPrec, int rightPrec) {
        this.name.unparse(writer, leftPrec, rightPrec);
        writer.print(" ");
        this.type.unparse(writer, leftPrec, rightPrec);
        if (!this.type.getNullable()) {
            // Default is nullable.
            writer.keyword("NOT NULL");
        }
        if (this.alias != null) {
            writer.print(" AS ");
            this.alias.unparse(writer, leftPrec, rightPrec);
        }
        if (this.comment != null) {
            writer.print(" COMMENT ");
            this.comment.unparse(writer, leftPrec, rightPrec);
        }
    }

    public SqlIdentifier getName() {
        return name;
    }

    public void setName(SqlIdentifier name) {
        this.name = name;
    }

    public SqlDataTypeSpec getType() {
        return type;
    }

    public void setType(SqlDataTypeSpec type) {
        this.type = type;
    }

    public SqlIdentifier getAlias() {
        return alias;
    }

    public void setAlias(SqlIdentifier alias) {
        this.alias = alias;
    }

    public SqlCharStringLiteral getComment() {
        return comment;
    }

    public void setComment(SqlCharStringLiteral comment) {
        this.comment = comment;
    }
}
