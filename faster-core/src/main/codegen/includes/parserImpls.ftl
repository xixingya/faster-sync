<#--
// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to you under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
-->

void TableColumn(TableCreationContext context) :
{
}
{
    (LOOKAHEAD(2)
        TableColumn2(context.columnList)
    |
        context.primaryKeyList = PrimaryKey()
    |
        UniqueKey(context.uniqueKeysList)
    |
        ComputedColumn(context)
    )
}

void ComputedColumn(TableCreationContext context) :
{
    SqlNode identifier;
    SqlNode expr;
    boolean hidden = false;
    SqlParserPos pos;
}
{
    identifier = SimpleIdentifier() {pos = getPos();}
    <AS>
    expr = Expression(ExprContext.ACCEPT_SUB_QUERY) {
        expr = SqlStdOperatorTable.AS.createCall(Span.of(identifier, expr).pos(), expr, identifier);
        context.columnList.add(expr);
    }
}

void TableColumn2(List<SqlNode> list) :
{
    SqlParserPos pos;
    SqlIdentifier name;
    SqlDataTypeSpec type;
    SqlCharStringLiteral comment = null;
}
{
    name = SimpleIdentifier()
    type = ExtendedDataType()
    [ <COMMENT> <QUOTED_STRING> {
        String p = SqlParserUtil.parseString(token.image);
        comment = SqlLiteral.createCharString(p, getPos());
    }]
    {
        SqlTableColumn tableColumn = new SqlTableColumn(name, type, comment, getPos());
        list.add(tableColumn);
    }
}

/**
* Different with {@link #DataType()}, we support a [ NULL | NOT NULL ] suffix syntax for both the
* collection element data type and the data type itself.
*
* <p>See {@link #SqlDataTypeSpec} for the syntax details of {@link #DataType()}.
*/
SqlDataTypeSpec ExtendedDataType() :
{
    SqlTypeNameSpec typeName;
    final Span s;
    boolean elementNullable = true;
    boolean nullable = true;
}
{
    <#-- #DataType does not take care of the nullable attribute. -->
    typeName = TypeName() {
        s = span();
    }
    (
        LOOKAHEAD(3)
        elementNullable = NullableOptDefaultTrue()
        typeName = ExtendedCollectionsTypeName(typeName, elementNullable)
    )*
    nullable = NullableOptDefaultTrue()
    {
        return new SqlDataTypeSpec(typeName, s.end(this)).withNullable(nullable);
    }
}


SqlCreate SqlCreateTable(Span s, boolean replace) :
{
    final SqlParserPos startPos = s.pos();
    SqlIdentifier tableName;
    SqlNodeList primaryKeyList = null;
    List<SqlNodeList> uniqueKeysList = null;
    SqlNodeList columnList = SqlNodeList.EMPTY;
    SqlCharStringLiteral comment = null;
    SqlNodeList propertyList = SqlNodeList.EMPTY;
    SqlParserPos pos = startPos;
}
{
    <TABLE>

    tableName = CompoundIdentifier()
    [
        <LPAREN> { pos = getPos(); TableCreationContext ctx = new TableCreationContext();}
        TableColumn(ctx)
        (
            <COMMA> TableColumn(ctx)
        )*
        {
            pos = pos.plus(getPos());
            columnList = new SqlNodeList(ctx.columnList, pos);
            primaryKeyList = ctx.primaryKeyList;
            uniqueKeysList = ctx.uniqueKeysList;
        }
        <RPAREN>
    ]
    [ <COMMENT> <QUOTED_STRING> {
        String p = SqlParserUtil.parseString(token.image);
        comment = SqlLiteral.createCharString(p, getPos());
    }]

    {
        return new SqlCreateTable(startPos.plus(getPos()),
                tableName,
                columnList,
                primaryKeyList,
                uniqueKeysList,
                propertyList,
                comment);
    }
}



/**
* A sql type name extended basic data type, it has a counterpart basic
* sql type name but always represents as a special alias compared with the standard name.
*
* <p>For example, STRING is synonym of VARCHAR(INT_MAX)
* and BYTES is synonym of VARBINARY(INT_MAX).
*/
SqlTypeNameSpec ExtendedSqlBasicTypeName() :
{
    final SqlTypeName typeName;
    final String typeAlias;
    int precision = -1;
}
{
    (
        <STRING> {
            typeName = SqlTypeName.VARCHAR;
            typeAlias = token.image;
            precision = Integer.MAX_VALUE;
        }
    |
        <BYTES> {
            typeName = SqlTypeName.VARBINARY;
            typeAlias = token.image;
            precision = Integer.MAX_VALUE;
        }
    )
    {
        return new ExtendedSqlBasicTypeNameSpec(typeAlias, typeName, precision, getPos());
    }
}

/*
* Parses collection type name that does not belong to standard SQL, i.e. ARRAY&lt;INT NOT NULL&gt;.
*/
SqlTypeNameSpec CustomizedCollectionsTypeName() :
{
    final SqlTypeName collectionTypeName;
    final SqlTypeNameSpec elementTypeName;
    boolean elementNullable = true;
}
{
    (
        <ARRAY> {
            collectionTypeName = SqlTypeName.ARRAY;
        }
    |
        <MULTISET> {
            collectionTypeName = SqlTypeName.MULTISET;
        }
    )
    <LT>
    elementTypeName = TypeName()
    elementNullable = NullableOptDefaultTrue()
    <GT>
    {
        return new ExtendedSqlCollectionTypeNameSpec(
            elementTypeName,
            elementNullable,
            collectionTypeName,
            false,
            getPos());
    }
}

/**
* Parse a collection type name, the input element type name may
* also be a collection type. Different with #CollectionsTypeName,
* the element type can have a [ NULL | NOT NULL ] suffix, default is NULL(nullable).
*/
SqlTypeNameSpec ExtendedCollectionsTypeName(
        SqlTypeNameSpec elementTypeName,
        boolean elementNullable) :
{
    final SqlTypeName collectionTypeName;
}
{
    (
        <MULTISET> { collectionTypeName = SqlTypeName.MULTISET; }
    |
         <ARRAY> { collectionTypeName = SqlTypeName.ARRAY; }
    )
    {
        return new ExtendedSqlCollectionTypeNameSpec(
             elementTypeName,
             elementNullable,
             collectionTypeName,
             true,
             getPos());
    }
}

/** Parses a SQL map type, e.g. MAP&lt;INT NOT NULL, VARCHAR NULL&gt;. */
SqlTypeNameSpec SqlMapTypeName() :
{
    SqlDataTypeSpec keyType;
    SqlDataTypeSpec valType;
    boolean nullable = true;
}
{
    <MAP>
    <LT>
    keyType = ExtendedDataType()
    <COMMA>
    valType = ExtendedDataType()
    <GT>
    {
        return new SqlMapTypeNameSpec(keyType, valType, getPos());
    }
}

/**
* Parse a "name1 type1 [ NULL | NOT NULL] [ comment ]
* [, name2 type2 [ NULL | NOT NULL] [ comment ] ]* ..." list.
* The comment and NULL syntax doest not belong to standard SQL.
*/
void ExtendedFieldNameTypeCommaList(
        List<SqlIdentifier> fieldNames,
        List<SqlDataTypeSpec> fieldTypes,
        List<SqlCharStringLiteral> comments) :
{
    SqlIdentifier fName;
    SqlDataTypeSpec fType;
    boolean nullable;
}
{
    [
        fName = SimpleIdentifier()
        fType = ExtendedDataType()
        {
            fieldNames.add(fName);
            fieldTypes.add(fType);
        }
        (
            <QUOTED_STRING> {
                String p = SqlParserUtil.parseString(token.image);
                comments.add(SqlLiteral.createCharString(p, getPos()));
            }
        |
            { comments.add(null); }
        )
    ]
    (
        <COMMA>
        fName = SimpleIdentifier()
        fType = ExtendedDataType()
        {
            fieldNames.add(fName);
            fieldTypes.add(fType);
        }
        (
            <QUOTED_STRING> {
                String p = SqlParserUtil.parseString(token.image);
                comments.add(SqlLiteral.createCharString(p, getPos()));
            }
        |
            { comments.add(null); }
        )
    )*
}

/**
* Parse Row type, we support both Row(name1 type1, name2 type2)
* and Row&lt;name1 type1, name2 type2&gt;.
* Every item type can have a suffix of `NULL` or `NOT NULL` to indicate if this type is nullable.
* i.e. Row(f0 int not null, f1 varchar null). Default is nullable.
*
* <p>The difference with {@link #SqlRowTypeName()}:
* <ul>
*   <li>Support comment syntax for every field</li>
*   <li>Field data type default is nullable</li>
*   <li>Support ROW type with empty fields, e.g. ROW()</li>
* </ul>
*/
SqlTypeNameSpec ExtendedSqlRowTypeName() :
{
    List<SqlIdentifier> fieldNames = new ArrayList<SqlIdentifier>();
    List<SqlDataTypeSpec> fieldTypes = new ArrayList<SqlDataTypeSpec>();
    List<SqlCharStringLiteral> comments = new ArrayList<SqlCharStringLiteral>();
    final boolean unparseAsStandard;
}
{
    <ROW>
    (
        <NE> { unparseAsStandard = false; }
    |
        <LT> ExtendedFieldNameTypeCommaList(fieldNames, fieldTypes, comments) <GT>
        { unparseAsStandard = false; }
    |
        <LPAREN> ExtendedFieldNameTypeCommaList(fieldNames, fieldTypes, comments) <RPAREN>
        { unparseAsStandard = true; }
    )
    {
        return new ExtendedSqlRowTypeNameSpec(
            getPos(),
            fieldNames,
            fieldTypes,
            comments,
            unparseAsStandard);
    }
}
void UniqueKey(List<SqlNodeList> list) :
{
    List<SqlNode> ukList = new ArrayList<SqlNode>();
    SqlParserPos pos;
    SqlIdentifier columnName;
}
{
    <UNIQUE> { pos = getPos(); } <LPAREN>
        columnName = SimpleIdentifier() { ukList.add(columnName); }
        (<COMMA> columnName = SimpleIdentifier() { ukList.add(columnName); })*
    <RPAREN>
    {
        SqlNodeList uk = new SqlNodeList(ukList, pos.plus(getPos()));
        list.add(uk);
    }
}

SqlNodeList PrimaryKey() :
{
    List<SqlNode> pkList = new ArrayList<SqlNode>();

    SqlParserPos pos;
    SqlIdentifier columnName;
}
{
    <PRIMARY> { pos = getPos(); } <KEY> <LPAREN>
        columnName = SimpleIdentifier() { pkList.add(columnName); }
        (<COMMA> columnName = SimpleIdentifier() { pkList.add(columnName); })*
    <RPAREN>
    {
        return new SqlNodeList(pkList, pos.plus(getPos()));
    }
}