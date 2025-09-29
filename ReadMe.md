# Faster Sync

全新一代的SQL处理框架，如果你有数据处理的需求，并且并没有想好使用什么做数据处理，那么，它将是你一种很好的选择。
Faster Sync 采用 Apache Calcite 做 SQL 的处理框架，并且自定义拓展了部分格式。

## Faster Sync能做什么？

1. **数据预处理**: 从埋点、kafka、log中的日志太多？可以使用 Faster Sync 来进行数据的预处理。你需要做的只是一个建表语句以及一个 SELECT 语句。
2. **非聚合特征的特征同步功能**: Faster Sync 作为一个轻量级的框架，可以满足绝大多数的非聚合特征的处理。

## 新版本特性 (v0.2)

- ✅ **Java 17 支持**: 升级到现代 Java 版本，提升性能和安全性
- ✅ **增强的线程安全**: 改进的同步机制，更好的并发处理
- ✅ **完善的错误处理**: 统一的异常处理和更详细的错误信息
- ✅ **优化的 JSON 处理**: 更好的 JSON 解析、验证和类型推断
- ✅ **资源管理**: 实现 AutoCloseable 接口，自动资源清理
- ✅ **代码质量提升**: 提取常量、改进日志记录、更好的文档

## 如何使用

### Maven 依赖

```xml
<dependency>
  <groupId>tech.xixing.sync</groupId>
  <artifactId>faster-core</artifactId>
  <version>0.2-SNAPSHOT</version>
</dependency>
```

### 基础使用示例

```java
// 方式1: 使用 CREATE 和 SELECT 语句
SQLConfig config = new SQLConfig(
    "create table t1(nickname string,uid bigint,varTimestamp bigint,status int)",
    "select uid||'_'||nickname as esId, varTimestamp as last_dis_conn_time, status as test_online from t1 where status = 1"
);

// 方式2: 使用预定义字段
Pair<String, LinkedHashMap<String, Object>> pair = SQLUtils.getTableConfigByCreateSql(
    "create table t1(nickname string,uid bigint,varTimestamp bigint,status int)"
);
SQLConfig config = new SQLConfig(
    "select uid||'_'||nickname as esId, varTimestamp as last_dis_conn_time, status as test_online from t1 where status = 1", 
    pair.left, 
    pair.right
);

// 创建转换器并处理数据
try (SQLTransformer sqlTransformer = new SQLTransformer(config)) {
    String jsonData = "[{\"uid\":12345,\"status\":1,\"varTimestamp\":1675141523785,\"nickname\":\"test\"}]";
    List<JSONObject> results = sqlTransformer.transform(jsonData);
    System.out.println(results);
}
```

### 高级特性

#### 自定义行转换器
```java
config.setRowConverter(new CustomRowConverter());
```

#### JSON 数据验证
```java
if (JsonUtils.isValidJson(jsonString)) {
    Optional<JSONArray> jsonArray = JsonUtils.parseJsonArray(jsonString);
    if (jsonArray.isPresent()) {
        // 处理有效的 JSON 数据
    }
}
```

## 架构改进

本版本对整体架构进行了重大改进：

- **配置管理**: 提取 SQLConfig 为专门的服务层，支持自动资源管理
- **线程安全**: 改进的同步机制，确保并发环境下的数据一致性
- **错误处理**: 统一的异常体系和详细的错误信息
- **JSON 处理**: 增强的 JSON 解析、类型推断和验证
- **代码质量**: 提取常量、改进日志记录、更好的代码组织

## 性能优化

- 优化了 JSON 解析性能
- 改进了内存使用效率
- 增强了错误恢复机制
- 更好的资源管理和清理

## 注意事项

- SQLTransformer 实例不是线程安全的，需要为每个线程创建独立实例
- 建议使用 try-with-resources 模式确保资源正确清理
- JSON 数据格式需要严格符合规范

## 贡献

欢迎提交 Issue 和 Pull Request 来改进这个项目。

## 许可证

Apache License 2.0
