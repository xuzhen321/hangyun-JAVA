# 航运管理系统 · 后端

基于《第六周报告》的关系模式实现的航运物流管理后端，覆盖**客户、订单、货物、集装箱、拖车、
港口、船舶、航次、物流事件、操作日志**等模块，提供 185 个 REST 接口。

---

## 技术栈

| 组件 | 版本 | 说明 |
|---|---|---|
| Java | 21 | |
| Spring Boot | 4.1.1 | ⚠️ 用 **Jackson 3**（包名 `tools.jackson`），和网上大量 Boot 3 的写法不兼容 |
| MyBatis-Plus | 3.5.17 | `mybatis-plus-spring-boot4-starter`（Boot 4 专用分支） |
| PostgreSQL | — | 全部 28 张表 + 7 个视图 |
| springdoc-openapi | 3.1.1 | Swagger UI |
| Apache POI | 5.3.0 | Excel 导出（纯 Java 库，不受 Boot 4 兼容性问题影响） |
| jjwt | 0.12.5 | JWT 签发与校验（0.12.x 的 API 与 0.11.x 完全不同） |
| spring-security-crypto | 由 Boot BOM 管理 | **只引这一个模块**做 BCrypt，不引整个 `spring-boot-starter-security` |

---

## 快速开始

### 1. 环境要求

- JDK 21
- Maven 3.9+
- PostgreSQL（本机 5432 端口）

### 2. 建库 + 配置数据库连接

```sql
create database demo;
```

数据库连接信息**不进仓库**（`application-dev.yaml` 已被 `.gitignore` 排除）。先复制模板：

```bash
cp src/main/resources/application-dev.example.yaml src/main/resources/application-dev.yaml
```

然后把里面的库名/用户名/密码换成你自己的。

### 3. 执行 SQL

> ⚠️ **顺序不能乱。** 完整说明见 [`sql必看.md`](sql必看.md) 第三、四节，这里只列最小可用路径。

```bash
DB=demo    # 改成你的库名

# ① 建表 + 触发器（只能跑一次，重复跑会报 relation already exists）
psql -U postgres -d $DB -f initial.sql

# ② 建视图（可重复执行）
psql -U postgres -d $DB -f viewInitial.sql

# ③ 内置状态（后面几个数据文件会引用它们）
psql -U postgres -d $DB -f customer-status-data.sql
psql -U postgres -d $DB -f order-status-data.sql

# ④ 登录账号 + 操作类型 —— ⚠️ 这两个必须有，否则登不进去
psql -U postgres -d $DB -f user-data.sql
psql -U postgres -d $DB -f operation-type-data.sql

# ⑤ 操作日志的"改前值"触发器（可重复执行）
#    ⚠️ 必须有：修改/删除的日志现在由它写，不跑的话这些操作**完全没有日志**
psql -U postgres -d $DB -f log-before-trigger.sql
```

到这里**已经可以登录和联调了**，但列表页和下拉框基本都是空的。要跑通端到端，把剩下的
**24 个测试数据文件**按 `sql必看.md` 第三节的顺序也跑一遍
（其中港口/船舶那七份小字典要排在 `port-data.sql` / `vessel-data.sql` 之前）。

> 📌 **库之前已经建过了？** 别重跑 `initial.sql`，看
> [`sql必看.md` 第六节「已经建过库了？增量变更」](sql必看.md)。

### 4. 启动

```bash
mvn spring-boot:run
```

默认端口 **8080**，无路径前缀（不是 `/api/v1`）。

### 5. 访问入口

| | 地址 |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |

---

## 默认账号

| 登录名 | 密码 | 说明 |
|---|---|---|
| `admin` | `admin123` | 内置管理员，**不可删除、不可冻结** |
| `zhangmin` | `123456` | 普通账号，演示用 |

⚠️ **这两个是演示用的弱口令，且写在仓库里的 `user-data.sql` 中，上线前务必改掉。**

改密码走接口（不要在库里直接 `update` 成明文 —— 密码列存的是 BCrypt 哈希，写明文会让账号登不进去）：

```
PUT /users/{id}/password   { "newPassword": "新密码" }
```

---

## ⚠️ 认证：所有接口都要带令牌

除 `POST /auth/login` 和 Swagger 相关路径外，**所有接口都要求**：

```http
Authorization: Bearer <token>
```

否则返回 **401** + `code=0` + `msg="登录已失效，请重新登录"`。

```bash
# 登录拿令牌
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 带上令牌调业务接口
curl http://localhost:8080/customers -H "Authorization: Bearer eyJhbGciOi..."
```

前端接入（axios 拦截器写法）见「前端接口-认证与日志」第 26.1 节。

> 📌 JWT 密钥在 `application.yaml` 的 `hangyun.jwt.secret`，**默认值是演示用的**。
> 生产环境用环境变量覆盖，不用改代码：
>
> ```bash
> set HANGYUN_JWT_SECRET=<你自己的随机串>     # Windows
> export HANGYUN_JWT_SECRET=<你自己的随机串>   # Linux / macOS
> ```
>
> ⚠️ 长度必须 **≥ 32 字节**，短了 jjwt 会在**签发时**才抛 `WeakKeyException`（启动不报错，一登录就炸）。

---

## 项目结构

```
src/main/java/com/test/hangyun/
├── auth/           登录令牌：JWT 签发/校验、拦截器、当前用户 ThreadLocal
├── common/         统一响应 Result / PageResult、全局异常处理
├── config/         跨域+拦截器配置、MyBatis-Plus 分页、Jackson 时间格式、密码器
├── constant/       各表的状态/枚举常量、分页与导出上限
├── controller/     29 个 REST 控制器
├── dto/            请求对象（XxxCreateReq / XxxUpdateReq / XxxQueryReq）
│   └── vo/         响应对象（XxxVO / XxxOptionVO）
├── excel/          Excel 导出：流式写（POI SXSSF）+ 统一响应头
├── log/            @OpLog 注解 + 操作日志切面 + 独立事务写入
├── mapper/         MyBatis-Plus Mapper（写基础表 / 读视图）
├── pojo/
│   ├── entity/     28 个基础表实体（写）
│   ├── enums/      枚举（OpType、EventSource、EstimateFlag）
│   └── view/       7 个视图实体（只读）
└── service/        29 个 Service + 实现
```

**规模**：318 个 Java 文件，29 个控制器，28 张表，7 个视图，185 个接口。

---

## 核心约定

写代码前值得先知道这几条（详细理由散在「后端接口设计文档」里）：

| 约定 | 说明 |
|---|---|
| **读写分离** | 查询走**视图**（`pojo/view`，联表已在库里做好，前端一次请求拿全）；写走**基础表**（`pojo/entity`） |
| **下拉框例外** | `/xxx/options` 读**基础表**，不读视图 —— 它只要本表字段，不值得为它跑多表 JOIN |
| **统一响应** | `{ code, msg, data }`，`code` 只有 `1` 成功 / `0` 失败；**错误类别由 HTTP 状态码承载**（400/401/403/404/409/500） |
| **删除分两种** | 主体（客户/订单/集装箱/港口/用户/物流事件）**逻辑删除**；字典和明细**物理删除 + 引用检查**（被引用则 409） |
| **时间** | 一律 `LocalDateTime`，JSON 输出 `yyyy-MM-dd HH:mm:ss`；`insert_time` / `update_time` 由**数据库触发器**维护，应用层不赋值 |
| **主键** | 25 张表自增（`IdType.AUTO`）；`Orders` / `Container.no` / `Trailer.no` 是业务字符串主键 |
| **操作日志** | 写方法标 `@OpLog`，切面自动记录（含失败操作）；**独立事务**，业务回滚不影响日志 |
| **导出接口** | 返回**文件流**不是 JSON（全项目唯一例外），5 万行上限 |

---

## 功能模块

| 模块 | 接口前缀 |
|---|---|
| 登录认证 | `/auth` |
| 系统用户 | `/users`（完整 CRUD + 重置密码 + 导出） |
| 客户 / 客户状态 | `/customers`、`/customer-statuses` |
| 订单 / 订单状态 | `/orders`、`/order-statuses` |
| 货物 / 货物种类 / 装箱结果 | `/cargos`、`/cargo-types`、`/cargo-container-results` |
| 集装箱 / 箱型 / 状态 | `/containers`、`/container-types`、`/container-statuses` |
| 拖车 / 提空箱登记 | `/trailers`、`/container-trailer-records` |
| 物流事件 / 事件状态 | `/container-events`、`/event-statuses` |
| 港口 / 区域 / 时区 / 尺寸 / 级别 / 类型 | `/ports`、`/areas`、`/timezones`、`/harbor-sizes`、`/port-levels`、`/port-types` |
| 船舶 / 船型 / 国家 | `/vessels`、`/ship-types`、`/countries` |
| 航次 | `/voyages` |
| 公司（通用字典） | `/companies` |
| 操作日志 / 操作类型 | `/logs`、`/operation-types` |

**Excel 导出**（`GET /{资源}/export`，筛选条件与列表一致）：`/logs`、`/users`、`/customers`、
`/orders`、`/cargos`、`/containers`、`/cargo-container-results`、`/container-trailer-records`、
`/container-events`、`/voyages`、`/vessels`、`/ports`。

> 📌 各种小字典（箱型 / 状态 / 区域 / 时区 / 级别 / 类型 / 国家 / 公司 / 拖车 / 船型 / 操作类型）
> **不做导出** —— 只有几行到几十行，导出没有意义。

---

## 文档索引

| 文档 | 内容 |
|---|---|
| [`sql必看.md`](sql必看.md) | **拿到代码先看这个**：SQL 执行顺序、可重复执行性、老库增量变更、装完怎么验证 |
| 前端接口对接文档 | **面向前端**：怎么连、统一响应格式、错误码、全部接口总表、踩坑清单 |
| 前端接口-客户 / 订单 / 货物 / 集装箱 / 公司 / 物流事件 / 船舶 / 港口 / 认证与日志（共 9 份分册） | 各模块的详细字段说明与错误码 |
| 后端接口设计文档 | **面向后端**：分层设计、读写分离、逻辑删除、日志切面、认证方案、以及设计与实现的出入记录 |

> 📌 除 `sql必看.md` 外，上面那几份文档**没有随本仓库发布**（都在本地开发目录里）。
> 接口的在线文档请直接看运行后的 Swagger：`http://localhost:8080/swagger-ui/index.html`。

---

## 常见问题

**Q：启动后调任何接口都报 `relation ... does not exist`？**
SQL 没跑全。按 `sql必看.md` 第三节的顺序补跑。

**Q：接口全返回 401？**
没带令牌。除 `/auth/login` 外都要带 `Authorization: Bearer <token>`。

**Q：登录报「用户名或密码错误」？**
先确认 `users` 表有数据（`select count(*) from users;` 应为 2）。若之前用 `update users set password='admin123'`
改成过明文，账号就再也登不进了（密码列要存 BCrypt 哈希），用 `user-data.sql` 文件头里的哈希修回来。

**Q：列表能出来但某些列是空的？**
区间/字典类的列靠 `on conflict do nothing` 是**补不上**的 —— 老行已存在，重跑数据文件不会更新它们。
对应文件头都附了手工 `update` 语句。

**Q：`GET /vessels`、`GET /ports` 报字段不存在？**
库里的视图是旧的。重跑 `viewInitial.sql`（它开头会先 `drop view`）。

**Q：报 `字段 "xxx" 不存在` 或 `视图列的名称从...改成...`？**
同上，视图需要重建。`create or replace view` **不允许在中间插入列**（PostgreSQL 按列位置匹配），
所以 `viewInitial.sql` 开头会先 `drop view`。

---

## 已知限制

- **登出无法在服务端强制失效**：JWT 是无状态的，登出只是前端丢弃令牌，令牌在过期前技术上仍可用。
  要做强制失效需引入黑名单或用户级令牌版本号。
- **修改/删除的日志没有「失败」记录**：这两类的改前/改后值由数据库触发器 `log_row_change()` 写
  （`log-before-trigger.sql`），它和业务在**同一个事务**里，操作失败时日志跟着回滚。
  新增/导出/登录仍由切面记、失败也留痕。「拿到改前值」和「失败也留痕」走触发器只能二选一，
  这是刻意的取舍，详见 `log-before-trigger.sql` 的文件头。
- **导出会先把整个文件生成到内存**再返回：换来的是出错时能返回干净的 JSON 错误，
  而不是一个打不开的损坏 xlsx。5 万行上限就是这个取舍的边界。
