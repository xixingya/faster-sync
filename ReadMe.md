# Faster Sync
全新一代的sql处理框架，如果你有数据处理的需求，
并且并没有想好使用什么做数据处理，那么，它将是你一种很好的选择。
Faster Sync 采用apache calcite做sql的处理框架，并且自定义拓展了部分格式。
# Faster Sync能做什么？
1. 数据预处理，从埋点、kafka、log中的日志太多？可以使用faster sync来进行数据的预处理。 你需要做的只是一个建表语句以及一个select语句。
2. 非聚合特征的特征同步功能，Faster Sync作为一个轻量级的框架，可以满足绝大多数的非聚合特征的处理。
# 如何使用：

```xml
<dependency>
  <groupId>tech.xixing.sync</groupId>
  <artifactId>faster-core</artifactId>
  <version>0.1</version>
</dependency>
```

```java
Pair<String,LinkedHashMap<String,Object>> pair = SQLUtils.getTableConfigByCreateSql("create table t1(nickname string,uid bigint,varTimestamp bigint,status int)");
SQLConfig config = new SQLConfig("select uid||'_'||nickname as esId, varTimestamp as last_dis_conn_time,status as test_online from t1 where status = 1", pair.left, pair.right);
SQLTransformer sqlTransformer = new SQLTransformer(config);
```
