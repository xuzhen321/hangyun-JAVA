# SQL 必看

> 拿到代码后，**按顺序把下面 4 个文件跑一遍**，后端就能用了。后面几个是测试数据，可选——但不装的话，列表页和下拉框都是空的，没法联调。
>
> ⚠️ **新增：接口现在要登录了**（除 `POST /auth/login` 和 Swagger，全都要带 `Authorization: Bearer <token>`）。
> 所以 **`user-data.sql` 也从"可选"变成了"要跑"** —— 不跑的话一个账号都没有，登录不进去，
> 而登录不进去就意味着**所有接口都调不通**。另外 `operation-type-data.sql` 要一起跑，
> 否则操作日志的"操作类型"那列是空的。

> ⚠️ **库已经建过、不是全新的？** 别重跑 `initial.sql`（第二次会报 `relation already exists`），
> 直接看 **第六节「已经建过库了？增量变更」**，照 **6.6 的升级清单**跑一遍。

---

## 一、必须执行的 5 个

| # | 文件 | 作用 | 不执行的后果 |
|---|---|---|---|
| 1 | `initial.sql` | 建 **28 张表** + 索引 + `set_time_fields()` 触发器函数 + 28 个触发器 | 应用**能启动**（Spring 不在启动时校验表结构），但**任何接口一调就 500**，日志里是 `relation "customer" does not exist` |
| 2 | `viewInitial.sql` | 建 **7 个视图** | 客户和订单的**列表、详情、修改、删除**全部报 `v_customer` / `v_order` 不存在（修改和删除虽然写的是基础表，但存在性校验查的是视图）；**只有新增和批量删除还能用**。另外 `GET /vessels`、`GET /ports` 会报**字段 `vessel_type_id` 不存在**（视图少了后补的 id 列，见 6.4） |
| 3 | `customer-status-data.sql` | 写入客户状态 **1 正常 / 2 异常 / 3 注销** | 客户状态下拉框是空的；客户删除（逻辑删除）会把状态指向不存在的 id |
| 4 | `order-status-data.sql` | 写入订单状态 **1 已确认 / 2 执行中 / 3 已完成 / 4 已取消** | 订单状态下拉框是空的；订单删除（逻辑删除）同上 |
| 5 | `log-before-trigger.sql` | 给 26 张业务表挂操作日志的**「改前值」触发器** `log_row_change()`（只挂 update / delete；函数用 `create or replace`、触发器先 drop 再建，**可重复执行**） | ⚠️ **修改和删除会完全没有日志** —— 这两类的日志现在由触发器写，切面已经不管了。日志列表里看不到任何 UPDATE / DELETE 记录 |

## 二、可选的 26 个

| 文件 | 作用 | 依赖 |
|---|---|---|
| `customer-data.sql` | 10 条客户样本数据 | 第 3 步（状态 1/2/3） |
| **↓ 下面七份是港口 / 船舶用到的字典，必须在 `port-data.sql` / `vessel-data.sql` 之前跑** | | |
| `country-data.sql` | 10 个国家（CN/US/JP…） | 第 1 步 |
| `area-data.sql` | 6 个区域（North/East/South China…） | 第 1 步 |
| `timezone-data.sql` | 6 个时区（两列都有唯一约束，一个 UTC 偏移只能有一条） | 第 1 步 |
| `harbor-size-data.sql` | 5 个港口规模档（Very Small → Very Large） | 第 1 步 |
| `port-level-data.sql` | 4 个港口级别（1~4，按年吞吐量分档：特大型/大型/中型/小型） | 第 1 步 |
| `port-type-data.sql` | 2 个港口类型 | 第 1 步 |
| `ship-type-data.sql` | 5 种船型（集装箱船/散货船/油轮/杂货船/滚装船） | 第 1 步 |
| **↑ 以上七份之间互相独立，谁先谁后都行** | | |
| `port-data.sql` | 10 条港口模拟数据（含 UN/LOCODE 和中英文名） | **上面七份字典 + 第 1 步** |
| `cargo-type-data.sql` | 12 条货物种类样本数据 | 第 1 步 |
| `order-data.sql` | 12 条订单样本数据 | **上面的客户 + 港口，以及第 4 步的状态** |
| `cargo-data.sql` | 14 条货物样本数据 | **货物种类 + 订单** |
| `container-type-data.sql` | 6 种箱型 | 第 1 步 |
| `container-status-data.sql` | 6 种集装箱状态（中英文两列） | 第 1 步 |
| `company-data.sql` | 8 家公司（箱主 / 操作方，含箱主代码） | 第 1 步 |
| `container-data.sql` | 6 个集装箱 | **上面三个字典** |
| `cargo-container-result-data.sql` | 3 条装箱结果样本数据 | **`cargo-data.sql` + `container-data.sql`** |
| `trailer-data.sql` | 8 辆拖车（车牌 + 司机） | 第 1 步 |
| `container-trailer-record-data.sql` | 8 条提空箱记录 | **`container-data.sql` + `trailer-data.sql`** |
| `event-status-data.sql` | 6 条事件状态（1-5 业务值 + 6 内置的"已删除"） | 第 1 步 |
| `vessel-data.sql` | 5 艘船（船名 / MMSI / IMO） | **`company-data.sql` + `country-data.sql` + `ship-type-data.sql`** |
| `voyage-data.sql` | 4 个航次 | **`port-data.sql` + `vessel-data.sql`** |
| `container-event-data.sql` | 10 条物流事件（含一条逻辑删除的） | **事件状态 + 集装箱 + 港口 + 航次** |
| **↓ 下面三个是"登录 + 操作日志"用的，不装的话登录不了** | | |
| `user-data.sql` | 2 个账号：`admin/admin123`、`zhangmin/123456`（**密码是 BCrypt 哈希**） | 第 1 步 |
| `operation-type-data.sql` | 5 个固定操作类型：1 `INSERT` / 2 `UPDATE` / 3 `DELETE` / 4 `EXPORT` / 5 `LOGIN` | 第 1 步 |
| `log-data.sql` | 8 条操作日志样本（含 1 条失败记录） | **`user-data.sql` + `operation-type-data.sql`** |

都是**纯测试数据，不装不影响功能**，但装了才能端到端联调：

- 不装 `customer-data.sql` → 客户列表是空的，没法选客户下单
- 不装 `port-data.sql` → `GET /ports/options` 返回 `[]`，起运港/目的港选不了
- 不装**那七份港口/船舶字典**（country / area / timezone / harbor-size / port-level / port-type / ship-type）→ 港口表单的六组下拉框、船舶表单的船型/船旗国下拉框**全是空的**；而且 `port-data.sql` / `vessel-data.sql` 填的那些字典 id 会全部悬空，列表里国家/区域/时区那几列显示为空
- 不装 `cargo-type-data.sql` → 货物种类列表是空的，名称搜索没东西可试
- 不装 `order-data.sql` → 订单列表是空的，`GET /customers/{id}/orders` 也查不到东西
- 不装 `cargo-data.sql` → `GET /cargos` 查不到东西；**货物种类的删除保护也测不出来**（没有货物引用它们，删谁都会成功）
- 不装**那三张集装箱字典** → 新增集装箱时，箱型 / 箱主 / 操作方 / 状态**四栏全都没有下拉选项**
- 不装 `container-data.sql` → `GET /containers` 是空的；而且**一条装箱结果都建不了**（箱号必须是已存在的集装箱，见下）
- 不装 `trailer-data.sql` → `GET /trailers` 是空的；而且**一条提空箱记录都建不了**（拖车号必须是已存在的拖车）

⚠️ **`cargo-container-result-data.sql` 有用处**：既让 `/cargo-container-results` 的列表和筛选有东西可试，又让**「订单货物的删除保护」可以被验证**——删货物前会检查有没有装箱记录引用它，表空着的话删任何货物都会成功，这道保护等于测不到。

装了之后**货物 1 和 3 就删不掉了**（这是有意的，不是 bug）。不需要验证删除保护的话可以不装，文件末尾附了撤销语句。

> 📌 那三个集装箱字典文件对应的完整资源（`/container-types`、`/container-statuses`、`/companies`）**都已经做了**（列表/详情/增删改 + `/options`，见 `后端接口设计文档.md` 5.2）。不装这几个文件的话，新增集装箱那几栏没有下拉选项可挑。

---

## 三、执行顺序

顺序**不能乱**，尤其是第 3 → 5 步：

```
1. initial.sql              建表 / 触发器 / 自增序列
        ↓
2. viewInitial.sql          建视图（依赖第 1 步的 28 张表）
        ↓
3. customer-status-data.sql 内置状态 1-3  ┐
4. order-status-data.sql    内置状态 1-4  ┘ 这两个谁先谁后都行
        ↓
5. customer-data.sql        客户样本（可选，引用第 3 步的 1/2/3）
        ↓
6. country-data.sql         国家 1-10    ┐
7. area-data.sql            区域 1-6     │
8. timezone-data.sql        时区 1-6     │ 港口/船舶的七份字典, 互相独立,
9. harbor-size-data.sql     尺寸 1-5     │ 谁先谁后都行 —— 但**必须在
10. port-level-data.sql     级别 1-4     │ 第 13 步(港口)和第 25 步(船舶)之前**
11. port-type-data.sql      类型 1-2     │
12. ship-type-data.sql      船型 1-5     ┘
        ↓
13. port-data.sql           港口样本（可选，**依赖第 6-11 步的六份字典**）
14. cargo-type-data.sql     货物种类样本（可选，只依赖第 1 步）
        ↓
15. order-data.sql          订单样本（可选，依赖第 4/5/13 步）
        ↓
16. cargo-data.sql          货物样本（可选，依赖第 14/15 步）
        ↓
17. container-type-data.sql   箱型 1-6   ┐
18. container-status-data.sql 状态 1-6   │ 三个字典互相独立, 谁先谁后都行
19. company-data.sql          公司 1-8   ┘
        ↓
20. container-data.sql      集装箱样本（可选，依赖第 17/18/19 步）
        ↓
21. cargo-container-result-data.sql   装箱结果样本（可选，依赖第 16/20 步）
        ↓
22. trailer-data.sql        拖车样本（可选，只依赖第 1 步）
        ↓
23. container-trailer-record-data.sql 提空箱记录样本（可选，依赖第 20/22 步）
        ↓
24. event-status-data.sql   事件状态 1-6（可选，只依赖第 1 步）
        ↓
25. vessel-data.sql         船舶样本（可选，依赖第 19 步公司 + 第 6 步国家 + 第 12 步船型）
        ↓
26. voyage-data.sql         航次样本（可选，依赖第 13 步港口 + 第 25 步船舶）
        ↓
27. container-event-data.sql 物流事件样本（可选，依赖第 24/20/13/26 步）
        ↓
28. user-data.sql            用户账号（可选，只依赖第 1 步）
29. operation-type-data.sql  操作类型 1-5 INSERT/UPDATE/DELETE/EXPORT/LOGIN（可选，只依赖第 1 步）
        ↓
30. log-data.sql             操作日志样本（可选，依赖第 28/29 步）
```

另有 **`log-before-trigger.sql`**（操作日志的「改前值」触发器，第一节的第 5 个），
它只依赖第 1 步建出来的 `log` 表，**放在第 1 步之后的任意位置都行**，不必占用上面的编号。

> ⚠️ **第 28 步不装的话登录不了** —— `POST /auth/login` 在 `Users` 表里找不到任何账号。
> 另外**拦截器上线后所有接口都要带令牌**，所以 28 步实际上已经是"要跑"而不是"可选"了。
>
> ⚠️ **库是早就建好的（不是全新库）的话，第 28~30 步之前必须先跑 6.7 的主键迁移** ——
> 那三个文件末尾的 `setval` 要求 `users` / `operation_type` / `log` 的 id 上已经有 identity 序列，
> 没迁移的话会报 `setval(NULL, ...)` 直接失败。

第 6-12 步只依赖第 1 步，放在第 5 步前后都行；第 14 步同理；第 17-19 步也同理。

⚠️ **第 6-12 步必须排在第 13 步（港口）和第 25 步（船舶）之前**：`port-data.sql` 里的 `country_id/area_id/timezone_id/…` 现在填的是这批字典的真实 id，`vessel-data.sql` 的 `vessel_type_id/country_id` 同理。反了的话港口和船舶的字典列全是悬空 id，**列表里那几列名称会是空的**。

⚠️ **第 21 步必须在第 20 步之后**：它引用的三个箱号（`CSNU1234567` 等）就在 `container-data.sql` 里。顺序反了的话，这些装箱记录指向的箱号在 `container` 表里不存在——而且**接口层现在有箱号存在性校验**，前端再想建同样的记录会被 400 拦住。

⚠️ **顺序错了不会报错，但会静默出脏数据** —— 库里没有物理外键：

- 第 5 步放在第 3 步之前 → 客户的 `status_id` 指向不存在的状态
- 第 15 步放在第 5、13 步之前 → 订单的 `customer_id` / 港口 id / `status_id` 全部悬空
- 第 13 步放在第 6-11 步之前 → 港口的六组字典 id 全部悬空，列表里国家/区域/时区那几列是空的

**判断标准**：被引用的数据先跑。`order-data.sql` 依赖三个文件，所以永远排最后。

## 四、怎么执行

前置条件：本机跑着 PostgreSQL，并且有一个数据库（默认配置里叫 `demo`，见 `src/main/resources/application-dev.yaml`）。

```bash
psql -h localhost -p 5432 -U postgres -d demo -f initial.sql
psql -h localhost -p 5432 -U postgres -d demo -f viewInitial.sql
psql -h localhost -p 5432 -U postgres -d demo -f customer-status-data.sql
psql -h localhost -p 5432 -U postgres -d demo -f order-status-data.sql
psql -h localhost -p 5432 -U postgres -d demo -f customer-data.sql   # 可选
psql -h localhost -p 5432 -U postgres -d demo -f port-data.sql       # 可选
psql -h localhost -p 5432 -U postgres -d demo -f cargo-type-data.sql # 可选
psql -h localhost -p 5432 -U postgres -d demo -f order-data.sql      # 可选
psql -h localhost -p 5432 -U postgres -d demo -f cargo-data.sql      # 可选
psql -h localhost -p 5432 -U postgres -d demo -f container-type-data.sql      # 可选
psql -h localhost -p 5432 -U postgres -d demo -f container-status-data.sql    # 可选
psql -h localhost -p 5432 -U postgres -d demo -f company-data.sql             # 可选
psql -h localhost -p 5432 -U postgres -d demo -f container-data.sql           # 可选
psql -h localhost -p 5432 -U postgres -d demo -f cargo-container-result-data.sql  # 可选
psql -h localhost -p 5432 -U postgres -d demo -f trailer-data.sql                 # 可选
psql -h localhost -p 5432 -U postgres -d demo -f container-trailer-record-data.sql # 可选
psql -h localhost -p 5432 -U postgres -d demo -f event-status-data.sql           # 可选
psql -h localhost -p 5432 -U postgres -d demo -f vessel-data.sql                 # 可选
psql -h localhost -p 5432 -U postgres -d demo -f voyage-data.sql                 # 可选
psql -h localhost -p 5432 -U postgres -d demo -f container-event-data.sql        # 可选
```

也可以在 Navicat / DataGrip 里按同样的顺序打开并运行。

> 库名不是 `demo` 的话，改 `application-dev.yaml` 里的 `spring.datasource.url` 即可。
> 每个文件内部都包了 `begin; ... commit;`，中途出错会整体回滚，不会留下一半。

## 五、能不能重复执行

| 文件 | 重复执行 | 说明 |
|---|---|---|
| `initial.sql` | ❌ **不能** | 用的是 `create table`（没有 `if not exists`），第二次跑会报 `relation already exists`。**只在全新库上跑一次** |
| `viewInitial.sql` | ✅ 可以 | 开头先 `drop view if exists` 再 `create`（**不能只用 `create or replace`**，原因见 6.4） |
| `customer-status-data.sql` | ✅ 可以 | `on conflict do nothing` |
| `order-status-data.sql` | ✅ 可以 | 同上 |
| `customer-data.sql` | ✅ 可以 | 同上 |
| `port-data.sql` | ✅ 可以 | 同上 |
| `cargo-type-data.sql` | ✅ 可以 | 同上 |
| `cargo-data.sql` | ✅ 可以 | 同上 |
| `container-type-data.sql` | ✅ 可以 | 同上 |
| `container-status-data.sql` | ✅ 可以 | 同上 |
| `company-data.sql` | ✅ 可以 | 同上 |
| `container-data.sql` | ✅ 可以 | 同上。另外它**不需要 setval** —— `container.no` 是 varchar 主键，没有自增序列 |
| `cargo-container-result-data.sql` | ✅ 可以 | 同上 |
| `trailer-data.sql` | ✅ 可以 | 同上。另外它**不需要 setval** —— `trailer.no` 是 varchar 主键，没有自增序列 |
| `container-trailer-record-data.sql` | ✅ 可以 | 同上 |
| `event-status-data.sql` | ✅ 可以 | 同上 |
| `country-data.sql` / `area-data.sql` / `timezone-data.sql` / `harbor-size-data.sql` / `port-level-data.sql` / `port-type-data.sql` / `ship-type-data.sql` | ✅ 可以 | 同上（那七份港口/船舶字典） |
| `vessel-data.sql` | ✅ 可以 | 同上 |
| `voyage-data.sql` | ✅ 可以 | 同上。⚠️ 它给航次填了 `vsl_id` —— **如果你之前已经跑过这个文件**，那几条航次已存在，`insert` 不会更新它们，要手工跑一遍文件头里那几条 `update` |
| `user-data.sql` | ✅ 可以 | 同上。末尾**有 setval**（`users.id` 现在自增了，显式插 id 不会推进序列） |
| `operation-type-data.sql` | ✅ 可以 | 同上，末尾**有 setval** |
| `log-data.sql` | ✅ 可以 | 同上，末尾**有 setval** |
| `container-event-data.sql` | ✅ 可以 | 同上 |
| `order-data.sql` | ✅ 可以 | 同上。另外它**不需要 setval** —— `orders.id` 是 varchar 主键，没有自增序列 |
| `voyage-unique-constraint.sql` | ✅ 可以 | 用 `do $$` 查过 `pg_constraint`，加过就跳过（见 6.3） |
| `log-before-trigger.sql` | ✅ 可以 | 函数用 `create or replace`；触发器**先 `drop trigger if exists` 再建**，所以**从"挂 insert 的旧版本"升级上来也能直接重跑**，不会报 `already exists` |

如果 `initial.sql` 跑到一半失败了，需要先把已建的表删掉再重跑，或者直接删库重建。

## 六、已经建过库了？增量变更

`initial.sql` 只能跑一次（见上一节），所以**库已经建过的话，下面这些改动不会自动生效**，要手动执行。

> 📌 另外，集装箱状态里新增了一条**内置的「已删除」(id=7)**——集装箱的"删除"就是改成它。
> 这条**不用手写 SQL**：**重跑一遍 `container-status-data.sql` 就行**（那个文件可重复执行，`on conflict do nothing`）。

### 6.1 两张表的业务列改为 not null

`cargo` 和 `cargo_container_result` 的"必需信息"列现在都是 `not null`（见 `initial.sql` 的编写约定第 7 条）。

> ⚠️ **执行前必须先确认没有空值**，否则 `set not null` 会直接报错中断：
>
> ```sql
> -- 两个都应该是 0
> select count(*) from cargo
> where cargo_type_id is null or order_id is null or quantity is null;
>
> select count(*) from cargo_container_result
> where cargo_id is null or container_no is null or quantity is null;
> ```
>
> 结果不是 0 的话，先把这些行补全或删掉。**装过旧版 `cargo-data.sql` 的话，里面 id=13 那行 `cargo_type_id` 是空的**，先执行：
>
> ```sql
> update cargo set cargo_type_id = 1 where cargo_type_id is null;
> ```

```sql
alter table cargo alter column cargo_type_id set not null;
alter table cargo alter column order_id      set not null;
alter table cargo alter column quantity      set not null;

alter table cargo_container_result alter column cargo_id     set not null;
alter table cargo_container_result alter column container_no set not null;
alter table cargo_container_result alter column quantity     set not null;
```

### 6.2 索引的增量变更

下面全部用了 `if not exists` / `drop if exists`，**重复执行也不会报错**，可以放心整段跑。

#### 港口的中英文名前缀索引（给 `/ports/options` 用）

```sql
create index if not exists idx_port_enname_pattern on Port (enname varchar_pattern_ops);
create index if not exists idx_port_cnname_pattern on Port (cnname varchar_pattern_ops);
```

#### 船舶 / 港口列表的前缀搜索索引（给 `/vessels`、`/ports` 的 `keyword` 用）

```sql
-- vessel: keyword 同时匹配 船名 / MMSI / IMO
create index if not exists idx_vessel_name_pattern   on Vessel (name varchar_pattern_ops);
create index if not exists idx_vessel_mmsi_pattern   on Vessel (mmsi varchar_pattern_ops);
create index if not exists idx_vessel_imo_pattern    on Vessel (imo varchar_pattern_ops);

-- port: keyword 还匹配五字码(中英文名上面那两条已经建过了)
create index if not exists idx_port_unlocode_pattern on Port (unlocode varchar_pattern_ops);
```

> `mmsi` / `imo` / `unlocode` 上原本就有 **UNIQUE** 索引，但那个服务等值查询，撑不起 `LIKE 'x%'`，所以另建一个 pattern 变体。
>
> 📌 其余那几张小字典（country / area / timezone / ship_type / harbor_size / port_level / port_type）最多十来行，前缀搜索直接全表扫就行，**故意不加索引**。

#### 货物种类的名称前缀索引（给 `/cargo-types` 的 name 搜索用）

```sql
create index if not exists idx_cargo_type_name_pattern on Cargo_Type (name varchar_pattern_ops);
```

#### 公司的名称 / 代码前缀索引（给 `/companies` 的 keyword 搜索用）

```sql
create index if not exists idx_company_name_pattern on Company (name varchar_pattern_ops);
create index if not exists idx_company_code_pattern on Company (code varchar_pattern_ops);
```

> 这两个索引是为了让"新增集装箱时选箱主/操作方"能搜出来，不用盲填 id。名称和代码都要建——`keyword` 是同时匹配这两个字段的。

#### 拖车 / 提空箱登记的前缀索引

```sql
-- 提空箱登记的列表按箱号 / 拖车号前缀搜。
-- 注意这两个列**已经有外键索引**了（idx_ctr_trailer_record_container_no / _track_no），
-- 但那个服务等值查询、服务不了 LIKE，所以同一列上再建一个 pattern 变体，各管一种查询。
create index if not exists idx_ctr_trailer_record_container_no_pattern on Container_Trailer_Record (container_no varchar_pattern_ops);
create index if not exists idx_ctr_trailer_record_track_no_pattern     on Container_Trailer_Record (track_no varchar_pattern_ops);

-- 拖车的列表和下拉框按拖车号 / 司机姓名前缀搜。拖车号是主键，普通索引同样撑不起 LIKE。
create index if not exists idx_trailer_no_pattern   on Trailer (no varchar_pattern_ops);
create index if not exists idx_trailer_name_pattern on Trailer (name varchar_pattern_ops);
```

#### 客户的姓名 / 资质前缀索引（给 `/customers` 的 name、qualification 搜索用）

```sql
drop index if exists idx_customer_name;
drop index if exists idx_customer_qualification;
create index idx_customer_name          on Customer (name varchar_pattern_ops);
create index idx_customer_qualification on Customer (qualification varchar_pattern_ops);
```

⚠️ 处理方式不同，别搞混：

| | 做法 |
|---|---|
| **客户**的 name / qualification | **重建** —— 原来的索引存在，但用的是默认 operator class，`LIKE 'x%'` 用不上，必须先 `drop` |
| **港口**的 enname / cnname、**货物种类**的 name | **新加** —— 直接 `create index` 即可 |

不补也不会报错，只是前缀搜索会退化成顺序扫描——数据量小的话感觉不出来。

确认一下建好没有：

```sql
select indexname from pg_indexes
where tablename in ('customer', 'port', 'cargo_type', 'company',
                    'trailer', 'container_trailer_record', 'vessel')
  and indexname like '%pattern%';
-- 期望 15 行:
--   idx_customer_name / idx_customer_qualification
--   idx_port_enname_pattern / idx_port_cnname_pattern / idx_port_unlocode_pattern
--   idx_vessel_name_pattern / idx_vessel_mmsi_pattern / idx_vessel_imo_pattern
--   idx_cargo_type_name_pattern
--   idx_company_name_pattern / idx_company_code_pattern
--   idx_trailer_no_pattern / idx_trailer_name_pattern
--   idx_ctr_trailer_record_container_no_pattern / idx_ctr_trailer_record_track_no_pattern
```

### 6.3 航次表的复合唯一约束 `(vsl_id, no)`

**整段直接跑 `voyage-unique-constraint.sql` 就行**（可重复执行，已经加过会自动跳过）。等价的手写语句是：

```sql
alter table Voyage add constraint uq_voyage_vsl_no unique (vsl_id, no);
```

> 📌 **为什么要这条约束**：原来航次号**什么都不查**，于是"同一条船 + 同一个航次号"能建出多条。
> 这种重复在界面上**分不出来**——物流事件视图里显示的就是「船名 + 航次号」，两条一模一样的航次
> 摆在下拉框/列表里，选错了不会报任何错，轨迹会静默挂到错的航次上。
>
> 📌 **为什么不给 `no` 建全库唯一**：航次号是**船公司自编**的，不同公司、不同年份大量重号
> （`001E` 这种每年复用一条）。全局唯一会挡住合法数据。所以唯一的是 **(船, 航次号) 这一对**。
>
> 📌 **`no` 还是可空的，不冲突**：PostgreSQL 的唯一约束**不比较 NULL**，所以
> `(null, '2026E001')` 这种行可以有任意多条。这和接口层"船或航次号任一为空就跳过查重"是同一口径。
>
> ⚠️ 如果加约束时报 `unique_violation`，说明库里**已经有**同船同号的重复航次，先查出来处理掉：
>
> ```sql
> select vsl_id, no, count(*), array_agg(id order by id) as ids
> from voyage
> where vsl_id is not null and no is not null
> group by vsl_id, no
> having count(*) > 1;
> -- 期望 0 行。有行的话，决定留哪条、改哪条的航次号或船舶，然后再重跑
> ```

不改库也能用——应用层同样会查重并返回 409（`voyage-unique-constraint.sql` 是让数据库也兜一道，
避免并发或手工 SQL 绕过校验）。**但接口层的 409 是新代码就有的，不影响前端联调。**

### 6.4 重建视图（**最容易漏，症状也最明显**）

**把 `viewInitial.sql` 整段重跑一遍就行**（文件开头已经加了 `drop view if exists`，可重复执行）。

> ⚠️ **不能只用 `create or replace view`**，会报这个错：
>
> ```
> [42P16] 错误: 不能将视图列的名称从"flag_state"改成"vessel_type_id"
> 建议: Use ALTER VIEW ... RENAME COLUMN ... to change name of view column instead.
> ```
>
> **原因**：PostgreSQL 的 `create or replace view` 是**按列的位置**匹配新旧视图的，不是按列名。
> 给 `v_vessel` 补的 4 个 id 列、给 `v_port` 补的 6 个 id 列**都插在中间**，
> 于是"第 6 列"从 `flag_state` 变成了 `vessel_type_id`，PostgreSQL 认为你在给列改名，拒绝执行。
>
> 报这个错之后还会跟着一串 `[25P02] 当前事务被终止, 事务块结束之前的查询被忽略` ——
> 那是连锁反应（一句失败，同一个事务里后面的全被忽略），**根因只有 42P16 那一条**。
>
> **正确做法**：先删再建。这 7 个视图互相没有依赖（都直接连基表），不用 `cascade`：
>
> ```sql
> drop view if exists v_customer, v_order, v_container_event,
>                      v_vessel, v_port, v_user, v_operation_log;
> ```
>
> 最新的 `viewInitial.sql` 已经把这句放在文件开头了，直接整段跑即可。

老库里这两个视图**少了后补的 id 列**：

| 视图 | 缺的列 | 谁在用 |
|---|---|---|
| `v_vessel` | `vessel_type_id`、`country_id`、`owner_company_id`、`manager_company_id` | `GET /vessels`（列表 / 详情 / 下拉框），也用来按船旗国、船型筛选 |
| `v_port` | `country_id`、`area_id`、`timezone_id`、`harbor_size_id`、`level_id`、`port_type_id` | `GET /ports`（列表 / 详情），也用来按国家筛选 |

> ⚠️ **不重建的症状**：`GET /vessels`、`GET /ports` 直接 **500**，日志里是
> `PSQLException: 错误: 字段 "vessel_type_id" 不存在`。
>
> 原因是视图**按定义缓存在库里**——代码按新列名发 SELECT，库里的视图还是旧的，
> 必然对不上。**光重启后端没用，必须重跑 `viewInitial.sql`。**

### 6.5 给老的港口 / 船舶数据补字典 id

`port-data.sql` 和 `vessel-data.sql` 现在会填六组 / 两组的字典 id，但它们用的是
`on conflict do nothing`——**你那 10 条港口、5 艘船早就存在了，重跑不会更新它们**，
字典 id 还是 null（列表里国家 / 区域 / 时区 / 船型那几列会显示为空）。

先确认是不是 null：

```sql
select id, unlocode, country_id, area_id, timezone_id from port order by id;
select id, name, vessel_type_id, country_id from vessel order by id;
```

是 null 的话，跑下面两段回填（前提：6-12 步那七份字典已经装好）：

```sql
-- 港口：10 条的六组字典 id
update port p set country_id     = x.country_id,
                  area_id        = x.area_id,
                  timezone_id    = x.timezone_id,
                  harbor_size_id = x.harbor_size_id,
                  level_id       = x.level_id,
                  port_type_id   = x.port_type_id
from (values
    (1,  1, 2, 1,            5, 1, 1),   -- 上海港
    (2,  1, 2, 1,            5, 1, 1),   -- 宁波港
    (3,  1, 3, 1,            5, 1, 1),   -- 深圳港
    (4,  1, 1, 1,            5, 2, 1),   -- 青岛港
    (5,  1, 1, 1,            5, 2, 1),   -- 天津港
    (6,  1, 3, 1,            4, 2, 1),   -- 厦门港
    (7,  1, 1, 1,            4, 2, 1),   -- 大连港
    (8,  1, 2, 1,            4, 3, 1),   -- 连云港港
    (9,  2, 5, 2,            5, 1, 1),   -- 洛杉矶港
    (10, 5, 4, null::bigint, 5, 1, 1)    -- 新加坡港(它是 UTC+8, 但那条时区已被上海占用)
) as x(id, country_id, area_id, timezone_id, harbor_size_id, level_id, port_type_id)
where p.id = x.id;

-- 船舶：5 艘的船型 + 船旗国（注意第 4 艘船型是 5、第 2/3/4 艘船旗国是 6，不是全都一样）
update vessel v set vessel_type_id = x.type_id, country_id = x.country_id
from (values
    (1, 1, 1),   -- 中远海运之星   集装箱船 / 中国
    (2, 1, 6),   -- 马士基哥本哈根 集装箱船 / 德国
    (3, 1, 6),   -- 地中海伊莎贝拉 集装箱船 / 德国
    (4, 5, 6),   -- 达飞雅克萨德   滚装船   / 德国
    (5, 1, 1)    -- 长荣之星       集装箱船 / 中国
) as x(id, type_id, country_id)
where v.id = x.id;
```

> 📌 这两段和 `port-data.sql` / `vessel-data.sql` 文件头里写的是同一份内容，跑一边就行。

### 6.6 老库升级清单（照这个顺序）

```sql
-- ① 七份字典数据（顺序随意，但必须在 ② 之前）
--    country-data.sql / area-data.sql / timezone-data.sql / harbor-size-data.sql
--    port-level-data.sql / port-type-data.sql / ship-type-data.sql

-- ② 重建视图（治 500，最容易漏）
--    viewInitial.sql

-- ③ 回填老数据的字典 id（见 6.5 的两段 update）

-- ④ 航次复合唯一约束（见 6.3）
--    voyage-unique-constraint.sql

-- ⑤ 可选：船舶/港口列表用的模式索引（见 6.2）

-- ⑥ 操作日志的"改前值"触发器（见 log-before-trigger.sql）
--    ⚠️ 不做的话修改/删除完全没有日志（切面已经不管了）
```

### 6.7 三张表的主键改成自增（**必须执行**）

`Users` / `Operation_Type` / `Log` 原来是从报告的"主键不自增"照抄下来的，
现在**改成了自增**，和后端实体上的 `@TableId(type = IdType.AUTO)` 对应。
**库和代码必须同时改**，只改一边会出错。

下面这段用 `do` 块先查 `pg_attribute.attidentity`，**重复执行也不会报错**，可以放心整段跑：

```sql
do $$
begin
    if not exists (select 1 from pg_attribute
                   where attrelid = 'users'::regclass and attname = 'id' and attidentity <> '') then
        alter table users alter column id add generated by default as identity;
    end if;
    if not exists (select 1 from pg_attribute
                   where attrelid = 'operation_type'::regclass and attname = 'id' and attidentity <> '') then
        alter table operation_type alter column id add generated by default as identity;
    end if;
    if not exists (select 1 from pg_attribute
                   where attrelid = 'log'::regclass and attname = 'id' and attidentity <> '') then
        alter table log alter column id add generated by default as identity;
    end if;
end $$;

-- ⚠️ 这一步不能省: 加 identity 时新建的序列是从 1 开始的, 而表里已经有 1/2(或 1~5、1~8),
--    不同步的话**下一次 INSERT 会直接撞主键**报 500。
select setval(pg_get_serial_sequence('users', 'id'),
              coalesce((select max(id) from users), 0) + 1, false);
select setval(pg_get_serial_sequence('operation_type', 'id'),
              coalesce((select max(id) from operation_type), 0) + 1, false);
select setval(pg_get_serial_sequence('log', 'id'),
              coalesce((select max(id) from log), 0) + 1, false);
```

确认一下三个序列都对：

```sql
select pg_get_serial_sequence('users', 'id')          as users_seq,
       pg_get_serial_sequence('operation_type', 'id') as optype_seq,
       pg_get_serial_sequence('log', 'id')            as log_seq;
-- 三个都不该是 null
```

> 📌 只跑那三个数据文件（它们末尾自带 `setval`）也能达到同样效果 —— 上面这段是给
> "库早就建好、但那几个数据文件还没跑"的情况兜底。

#### ⚠️ 执行顺序：**必须先跑 6.7 的迁移，再跑那三个数据文件**

`user-data.sql` / `operation-type-data.sql` / `log-data.sql` 末尾的
`setval(pg_get_serial_sequence('表','id'), ...)` **要求该列上已经有 identity 序列**。

如果库还没迁移（列仍是 `bigint primary key`，没有序列），`pg_get_serial_sequence` 会返回
**NULL**，`setval(NULL, ...)` 直接报错，整个事务回滚。

**正确顺序：**

```
6.7 的迁移语句  →  user-data.sql / operation-type-data.sql / log-data.sql
```

**反过来也别忘了**：新代码（`@TableId(type = IdType.AUTO)`）要求**库里必须有序列**。
只改代码不迁移库的话，**新增用户和写操作日志都会失败**（插入时 id 没有默认值，
报 `null value in column "id" violates not-null constraint`）—— 而且日志写失败的表现是
"业务操作成功了但接口报 500"，很难查。**库和代码必须同时改。**

## 七、装完怎么确认

几条只读 SQL，跑一下看数字对不对：

```sql
-- 应该是 28
select count(*) from information_schema.tables
where table_schema = 'public' and table_type = 'BASE TABLE';

-- 应该是 7
select count(*) from information_schema.views where table_schema = 'public';

-- 应该 3 行: 1 正常 / 2 异常 / 3 注销
select id, description from customer_status order by id;

-- 应该 4 行: 1 已确认 / 2 执行中 / 3 已完成 / 4 已取消
select id, description from order_status order by id;

-- 装了 customer-data.sql 的话应该是 10
select count(*) from customer;

-- 装了 port-data.sql 的话应该是 10
select count(*) from port;

-- 装了 order-data.sql 的话应该是 12
select count(*) from orders;

-- 装了 cargo-type-data.sql 的话应该是 12
select count(*) from cargo_type;

-- 装了 cargo-data.sql 的话应该是 14
select count(*) from cargo;

-- 装了 cargo-container-result-data.sql 的话应该是 3
select count(*) from cargo_container_result;

-- 集装箱那几份: 6 / 7 / 8 / 6
select count(*) from container_type;
select count(*) from container_status;   -- 7 条: 1-6 是示例值, 7 是内置的「已删除」
select count(*) from company;
select count(*) from container;

-- 拖车 8 辆、提空箱记录 8 条
select count(*) from trailer;
select count(*) from container_trailer_record;

-- 事件状态 6 条、船舶 5 艘、航次 4 个、物流事件 10 条
select count(*) from event_status;
select count(*) from vessel;
select count(*) from voyage;
select count(*) from container_event;

-- 航次的 vsl_id 应该都有值了（不是 null），否则轨迹里船名还是空的
select id, no, vsl_id from voyage order by id;

-- 七份港口/船舶字典: 10 / 6 / 6 / 5 / 3 / 2 / 5
select count(*) from country;
select count(*) from area;
select count(*) from timezone;
select count(*) from harbor_size;
select count(*) from port_level;
select count(*) from port_type;
select count(*) from ship_type;

-- 视图的列补上了没有? 期望 4 行
--   v_port   -> area_id, country_id
--   v_vessel -> country_id, vessel_type_id
-- 少行的话 GET /vessels、GET /ports 会 500, 见 6.4
select table_name, column_name from information_schema.columns
where (table_name = 'v_vessel' and column_name in ('vessel_type_id', 'country_id'))
   or (table_name = 'v_port'   and column_name in ('country_id', 'area_id'))
order by table_name, column_name;

-- 登录 + 操作日志那三张表: 期望 2 / 5 / 8
select count(*) from users;
select count(*) from operation_type;
select count(*) from log;

-- 三张表的主键序列都建好了吗? 期望三个都不为 null(见 6.7)
select pg_get_serial_sequence('users', 'id')          as users_seq,
       pg_get_serial_sequence('operation_type', 'id') as optype_seq,
       pg_get_serial_sequence('log', 'id')            as log_seq;

-- 初始账号能登录的前提: 密码必须是 BCrypt 哈希(以 $2a$ 开头), 不能是明文
select id, username, left(password, 7) as hash_prefix, status from users order by id;
-- 期望 hash_prefix 是 $2a$10$，status 都是 0

-- 航次的复合唯一约束在不在？期望 1 行(uq_voyage_vsl_no)。
-- 0 行说明你的库是 6.3 之前建的、还没跑过 voyage-unique-constraint.sql
-- （不跑也能用，接口层照样会查重返回 409）
select conname from pg_constraint where conname = 'uq_voyage_vsl_no';

-- 有没有同船同号的重复航次？期望 0 行
select vsl_id, no, count(*) from voyage
where vsl_id is not null and no is not null
group by vsl_id, no having count(*) > 1;

-- 操作日志的"改前值"触发器：期望正好 26 行
-- （每个触发器都应该是 `AFTER UPDATE OR DELETE`，**没有 INSERT**）
select event_object_table, event_manipulation from information_schema.triggers
where trigger_name like 'trg_log_change%' order by 1, 2;
```

再启动应用，打开 `http://localhost:8080/swagger-ui/index.html`，调一下 `GET /customer-statuses/all` 能返回 3 条就说明前 5 步都到位了。

## 八、几个容易踩的点

1. **顺序不能乱**，尤其 `customer-data.sql` 必须在 `customer-status-data.sql` 之后（见第三节）。
2. **`initial.sql` 只能跑一次**，其他几个可以反复跑。
3. **不要手工改内置状态的 id**。1-3（客户）和 1-4（订单）被 Java 代码写死依赖（`CustomerStatusConstants` / `OrderStatusConstants`），其中「注销」「已取消」还是逻辑删除的落点。这些状态在当前版本里**既不能改也不能删**（接口会返回 409）。
4. **`insert_time` / `update_time` 不要手工填**，`initial.sql` 里的触发器会自动维护，应用层也一律留空。
5. **别再给 UPDATE / DELETE 方法标 `@OpLog`** —— 这类操作的日志现在由 `log-before-trigger.sql` 的触发器写，标回去会让同一次修改**记两行**（触发器一条带 `before_value`、切面一条没有）。切面只负责 `INSERT` / `EXPORT` / `LOGIN`，以及 `Users` 表的写操作。
