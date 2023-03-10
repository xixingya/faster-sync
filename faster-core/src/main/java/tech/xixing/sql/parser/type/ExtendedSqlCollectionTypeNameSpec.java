package tech.xixing.sql.parser.type;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.sql.SqlCollectionTypeNameSpec;
import org.apache.calcite.sql.SqlTypeNameSpec;
import org.apache.calcite.sql.SqlWriter;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.util.Litmus;
import org.apache.calcite.util.Util;

/**
 * A extended sql type name specification of collection type,
 * different with {@link SqlCollectionTypeNameSpec},
 * we support NULL or NOT NULL suffix for the element type name(this syntax
 * does not belong to standard SQL).
 */
public class ExtendedSqlCollectionTypeNameSpec extends SqlCollectionTypeNameSpec {
    private final boolean elementNullable;
    private final SqlTypeName collectionTypeName;
    private final boolean unparseAsStandard;

    /**
     * Creates a {@code ExtendedSqlCollectionTypeNameSpec}.
     *
     * @param elementTypeName    element type name specification
     * @param elementNullable    flag indicating if the element type is nullable
     * @param collectionTypeName collection type name
     * @param unparseAsStandard  if we should unparse the collection type as standard SQL
     *                           style
     * @param pos                the parser position
     */
    public ExtendedSqlCollectionTypeNameSpec(
            SqlTypeNameSpec elementTypeName,
            boolean elementNullable,
            SqlTypeName collectionTypeName,
            boolean unparseAsStandard,
            SqlParserPos pos) {
        super(elementTypeName, collectionTypeName, pos);
        this.elementNullable = elementNullable;
        this.collectionTypeName = collectionTypeName;
        this.unparseAsStandard = unparseAsStandard;
    }

    @Override
    public RelDataType deriveType(SqlValidator validator) {
        RelDataType elementType = getElementTypeName().deriveType(validator);
        elementType = validator.getTypeFactory()
                .createTypeWithNullability(elementType, elementNullable);
        return createCollectionType(elementType, validator.getTypeFactory());
    }

    @Override
    public void unparse(SqlWriter writer, int leftPrec, int rightPrec) {
        if (unparseAsStandard) {
            this.getElementTypeName().unparse(writer, leftPrec, rightPrec);
            // Default is nullable.
            if (!elementNullable) {
                writer.keyword("NOT NULL");
            }
            writer.keyword(collectionTypeName.name());
        } else {
            writer.keyword(collectionTypeName.name());
            SqlWriter.Frame frame = writer.startList(SqlWriter.FrameTypeEnum.FUN_CALL, "<", ">");

            getElementTypeName().unparse(writer, leftPrec, rightPrec);
            // Default is nullable.
            if (!elementNullable) {
                writer.keyword("NOT NULL");
            }
            writer.endList(frame);
        }
    }

    @Override
    public boolean equalsDeep(SqlTypeNameSpec spec, Litmus litmus) {
        if (!(spec instanceof ExtendedSqlCollectionTypeNameSpec)) {
            return litmus.fail("{} != {}", this, spec);
        }
        ExtendedSqlCollectionTypeNameSpec that = (ExtendedSqlCollectionTypeNameSpec) spec;
        if (this.elementNullable != that.elementNullable) {
            return litmus.fail("{} != {}", this, spec);
        }
        return super.equalsDeep(spec, litmus);
    }

    //~ Tools ------------------------------------------------------------------

    /**
     * Create collection data type.
     *
     * @param elementType Type of the collection element
     * @param typeFactory Type factory
     * @return The collection data type, or throw exception if the collection
     *         type name does not belong to {@code SqlTypeName} enumerations
     */
    private RelDataType createCollectionType(RelDataType elementType,
                                             RelDataTypeFactory typeFactory) {
        switch (collectionTypeName) {
            case MULTISET:
                return typeFactory.createMultisetType(elementType, -1);
            case ARRAY:
                return typeFactory.createArrayType(elementType, -1);

            default:
                throw Util.unexpected(collectionTypeName);
        }
    }

    public boolean isElementNullable() {
        return elementNullable;
    }

    public SqlTypeName getCollectionTypeName() {
        return collectionTypeName;
    }

    public boolean isUnparseAsStandard() {
        return unparseAsStandard;
    }
}
