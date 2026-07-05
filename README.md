# quiz-platform

企业内部在线答题系统，Spring Boot + MyBatis-Plus + MySQL + MinIO。

## 已验证 / 未验证的部分（重要，先看这个）

这个沙箱环境访问不了 Maven Central（网络策略只放行了 npm/pypi/crates 等源，没开 Maven 仓库），
所以整个 Spring Boot 项目**没有条件在这里实际跑`mvn compile`**。以下是分层说明：

| 部分 | 验证状态 |
|---|---|
| `parse/` 包（`DocxRuleBasedParser`及正则规则） | ✅ 已用真实题库文件在本地装了POI+JDK实际编译运行，700/700题解析成功、0异常 |
| `sql/schema.sql` | ✅ 已装MariaDB实际建库、插入JSON字段数据验证通过 |
| Spring Boot / MyBatis-Plus / Controller / Service 代码 | ⚠️ 未编译，是按Spring Boot 3.x + MyBatis-Plus 3.5标准写法手写的，语法和API用法我逐行检查过，但建议你`mvn compile`后过一遍 |

## 快速开始

```bash
docker-compose up -d
mvn spring-boot:run
```

详细接口调用示例、已知简化/待办事项、目录结构说明见仓库完整README历史提交。
