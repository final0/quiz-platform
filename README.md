# quiz-platform

企业内部在线答题系统，Spring Boot + MyBatis-Plus + MySQL + MinIO，原生HTML/CSS/JS前端（无需单独构建，Spring Boot直接托管静态资源）。

## 已验证 / 未验证的部分（重要，先看这个）

这个沙箱环境访问不了 Maven Central（网络策略只放行了 npm/pypi/crates 等源，没开 Maven 仓库），
所以整个 Spring Boot 项目**没有条件在这里实际跑 `mvn compile`**。以下是分层说明：

| 部分 | 验证状态 |
|---|---|
| `parse/` 包（`DocxRuleBasedParser`及正则规则） | ✅ 已用真实题库文件在本地装了POI+JDK实际编译运行，700/700题解析成功、0异常 |
| `sql/schema.sql` | ✅ 已装MariaDB实际建库、插入JSON字段数据验证通过 |
| `src/main/resources/static/` 前端7个页面 | ✅ 已用jsdom做过语法检查+运行时冒烟测试（mock接口后模拟加载全部页面）+ exam.html完整交互流程端到端测试（开始答题→勾选作答→交卷→看结果全部走通） |
| Spring Boot / MyBatis-Plus / Controller / Service 代码 | ⚠️ 未编译，是按Spring Boot 3.x + MyBatis-Plus 3.5标准写法手写的，语法和API用法逐行检查过，但建议 `mvn compile` 后过一遍 |

## 快速开始

```bash
# 1. 起依赖（MySQL + MinIO，schema.sql会在MySQL容器首次启动时自动执行）
docker-compose up -d

# 2. 跑起来
mvn spring-boot:run

# 3. 浏览器打开 http://localhost:8080 即可看到前端页面，不需要额外起前端服务
```

## 前端页面说明

前端是纯静态 HTML/CSS/JS，放在 `src/main/resources/static/`，Spring Boot 默认会把这个目录当静态资源直接对外提供，
所以**不需要 npm/webpack 之类的构建步骤**，也不需要单独部署一个前端服务——这跟"内部轻量使用"的定位是一致的。

| 页面 | 路径 | 功能 |
|---|---|---|
| 首页 | `index.html` | 导航入口 |
| 题库管理 | `banks.html` | 创建/删除题库，列表 |
| 导入题库 | `import.html?bankId=` | 上传docx，展示解析统计（总数/高置信度/低置信度/异常段落） |
| 审核题目 | `review.html?bankId=` | 分页查看待审核题目，支持行内编辑题干/选项/答案后保存并通过、批量通过、驳回 |
| 试卷管理 | `papers.html` | 配置随机组卷规则（各题型数量+分值），列表，进入考试 |
| 答题 | `exam.html?paperId=` | 开始考试→倒计时→作答→交卷→查看逐题解析 |
| 错题本 | `wrongbook.html` | 查看历史错题（含答案+解析），标记已掌握 |

因为后端登录鉴权还没接（见下面"已知简化"），前端右上角"切换身份"是用 `localStorage` 存一个 `userId`/`deptId`，
所有接口调用都带着这个身份，不是真正的账号系统，接入JWT/Session后把 `js/common.js` 里的 `Session` 对象换掉即可，页面调用方式不用改。

## 接口调用示例（用Postman或curl）

```bash
# 建题库
curl -X POST http://localhost:8080/api/bank -H "Content-Type: application/json" \
  -d '{"name":"中国近现代史纲要客观题题库","deptId":1,"sourceType":"IMPORT","creatorId":1}'

# 上传docx解析
curl -X POST http://localhost:8080/api/import/docx \
  -F "file=@你的题库.docx" -F "bankId=1" -F "operatorId=1"

# 查看导入任务详情（解析统计）
curl "http://localhost:8080/api/import/task/1"

# 查看待审核题目
curl "http://localhost:8080/api/review/pending?bankId=1&page=1&size=20"

# 批量通过
curl -X POST http://localhost:8080/api/review/batch-approve -H "Content-Type: application/json" -d '[1,2,3]'

# 建试卷（随机组卷模式）
curl -X POST http://localhost:8080/api/paper -H "Content-Type: application/json" -d '{
  "name":"近代史纲要模拟考试",
  "bankId":1,"deptId":1,"mode":"EXAM","composeStrategy":"RANDOM",
  "singleCount":10,"singleScore":2,
  "judgeCount":5,"judgeScore":1,
  "durationMinutes":30,"passScore":60,"status":1,"creatorId":1
}'

# 开始考试
curl -X POST "http://localhost:8080/api/exam/start?paperId=1&userId=1"

# 交卷
curl -X POST "http://localhost:8080/api/exam/submit?userId=1" -H "Content-Type: application/json" -d '{
  "recordId":1,
  "answers":[{"questionId":1,"answer":"D"},{"questionId":2,"answer":"对"}]
}'
```

## 已知的简化 / 待办（对应"内部轻量使用，不考虑并发"）

- **登录鉴权未接入**：`operatorId`/`userId` 目前都靠接口参数直接传（前端用localStorage模拟身份），接JWT/Session后替换掉即可。
- **AI解析未实现**：`QuestionImportServiceImpl.doParse()` 里 `parseMode=AI` 分支现在直接抛异常，占好了扩展位置。
- **简答题阅卷**：自动判分只做了单选/多选/判断，简答题`isCorrect`/`score`留null代表"待人工阅卷"，阅卷补分接口还没写。
- **随机抽题是内存shuffle**：题库几千题这个量级没问题，几十万道题需要换成数据库层面的随机采样。
- **没做并发控制**：比如同一个人同时开两个考试session没做互斥限制。
- **固定试卷(FIXED组卷)只有后端接口**：前端只做了随机组卷(RANDOM)的创建表单，固定试卷需要直接调 `exam_paper_question` 相关接口或后续补前端。

## 目录结构

```
com.quiz.platform
├── common          # 统一返回体、异常处理、枚举
├── config          # MinIO客户端、MyBatis-Plus自动填充
├── entity          # 11张表对应的实体类
├── mapper          # MyBatis-Plus BaseMapper
├── parse           # 题库解析引擎（已验证）
├── service/impl    # 核心业务逻辑
└── controller      # REST接口

src/main/resources/static   # 前端页面（已验证，见上表）
```
