# SQL 必看

> 拿到代码后，**按顺序把下面 4 个文件跑一遍**，后端就能用了。后面几个是测试数据，可选——但不装的话，列表页和下拉框都是空的，没法联调。

---

## 一、必须执行的 4 个

| # | 文件 | 作用 | 不执行的后果 |
|---|---|---|---|
| 1 | `initial.sql` | 建 **28 张表** + 索引 + `set_time_fields()` 触发器函数 + 28 个触发器 | 应用**能启动**（Spring 不在启动时校验表结构），但**任何接口一调就 500**，日志里是 `relation "customer" does not exist` |
| 2 | `viewInitial.sql` | 建 **7 个视图** | 客户和订单的**列表、详情、修改、删除**全部报 `v_customer` / `v_order` 不存在（修改和删除虽然写的是基础表，但存在性校验查的是视图）；**只有新增和批量删除还能用** |
| 3 | `customer-status-data.sql` | 写入客户状态 **1 正常 / 2 异常 / 3 注销** | 客户状态下拉框是空的；客户删除（逻辑删除）会把状态指向不存在的 id |
| 4 | `order-status-data.sql` | 写入订单状态 **1 已确认 / 2 执行中 / 3 已完成 / 4 已取消** | 订单状态下拉框是空的；订单删除（逻辑删除）同上 |

## 二、可选的 16 个

| 文件 | 作用 | 依赖 |
|---|---|---|
| `customer-data.sql` | 10 条客户样本数据 | 第 3 步（状态 1/2/3） |
| `port-data.sql` | 10 条港口模拟数据（含 UN/LOCODE 和中英文名） | 第 1 步 |
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
| `vessel-data.sql` | 5 艘船（船名 / MMSI / IMO） | **`company-data.sql`** |
| `voyage-data.sql` | 4 个航次 | **`port-data.sql` + `vessel-data.sql`** |
| `container-event-data.sql` | 10 条物流事件（含一条逻辑删除的） | **事件状态 + 集装箱 + 港口 + 航次** |

都是**纯测试数据，不装不影响功能**，但装了才能端到端联调：

- 不装 `customer-data.sql` → 客户列表是空的，没法选客户下单
- 不装 `port-data.sql` → `GET /ports/options` 返回 `[]`，起运港/目的港选不了
- 不装 `cargo-type-data.sql` → 货物种类列表是空的，名称搜索没东西可试
- 不装 `order-data.sql` → 订单列表是空的，`GET /customers/{id}/orders` 也查不到东西
- 不装 `cargo-data.sql` → `GET /cargos` 查不到东西；**货物种类的删除保护也测不出来**（没有货物引用它们，删谁都会成功）
- 不装**那三张集装箱字典** → 新增集装箱时，箱型 / 箱主 / 操作方 / 状态**四栏全都没有下拉选项**
- 不装 `container-data.sql` → `GET /containers` 是空的；而且**一条装箱结果都建不了**（箱号必须是已存在的集装箱，见下）
- 不装 `trailer-data.sql` → `GET /trailers` 是空的；而且**一条提空箱记录都建不了**（拖车号必须是已存在的拖车）

⚠️ **`cargo-container-result-data.sql` 有用处**：既让 `/cargo-container-results` 的列表和筛选有东西可试，又让**「订单货物的删除保护」可以被验证**——删货物前会检查有没有装箱记录引用它，表空着的话删任何货物都会成功，这道保护等于测不到。

装了之后**货物 1 和 3 就删不掉了**（这是有意的，不是 bug）。不需要验证删除保护的话可以不装，文件末尾附了撤销语句。

> 📌 那三个集装箱字典文件**对应的完整资源（`/container-types`、`/container-statuses`、`/companies`）都还没做**——没有增删改接口。这三个文件只是把字典数据备好，让新增集装箱那几栏有东西可选。

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
6. port-data.sql            港口样本（可选，只依赖第 1 步）
7. cargo-type-data.sql      货物种类样本（可选，只依赖第 1 步）
        ↓
8. order-data.sql           订单样本（可选，依赖第 4/5/6 步）
        ↓
9. cargo-data.sql           货物样本（可选，依赖第 7/8 步）
        ↓
10. container-type-data.sql   箱型 1-6   ┐
11. container-status-data.sql 状态 1-6   │ 三个字典互相独立, 谁先谁后都行
12. company-data.sql          公司 1-8   ┘
        ↓
13. container-data.sql      集装箱样本（可选，依赖第 10/11/12 步）
        ↓
14. cargo-container-result-data.sql   装箱结果样本（可选，依赖第 9/13 步）
        ↓
15. trailer-data.sql        拖车样本（可选，只依赖第 1 步）
        ↓
16. container-trailer-record-data.sql 提空箱记录样本（可选，依赖第 13/15 步）
        ↓
17. event-status-data.sql   事件状态 1-6（可选，只依赖第 1 步）
        ↓
18. vessel-data.sql         船舶样本（可选，依赖第 12 步的公司）
        ↓
19. voyage-data.sql         航次样本（可选，依赖第 6 步的港口 + 第 18 步的船舶）
        ↓
20. container-event-data.sql 物流事件样本（可选，依赖第 8/17/19 步）
```

第 6、7 步只依赖第 1 步，放在第 5 步前后都行；第 10-12 步同理，放在第 5 步前后都行。

⚠️ **第 14 步必须在第 13 步之后**：它引用的三个箱号（`CSNU1234567` 等）就在 `container-data.sql` 里。顺序反了的话，这些装箱记录指向的箱号在 `container` 表里不存在——而且**接口层现在有箱号存在性校验**，前端再想建同样的记录会被 400 拦住。

⚠️ **顺序错了不会报错，但会静默出脏数据** —— 库里没有物理外键：

- 第 5 步放在第 3 步之前 → 客户的 `status_id` 指向不存在的状态
- 第 7 步放在第 5、6 步之前 → 订单的 `customer_id` / 港口 id / `status_id` 全部悬空

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
| `viewInitial.sql` | ✅ 可以 | 用的是 `create or replace view` |
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
| `vessel-data.sql` | ✅ 可以 | 同上 |
| `voyage-data.sql` | ✅ 可以 | 同上。⚠️ 它给航次填了 `vsl_id` —— **如果你之前已经跑过这个文件**，那几条航次已存在，`insert` 不会更新它们，要手工跑一遍文件头里那几条 `update` |
| `container-event-data.sql` | ✅ 可以 | 同上 |
| `order-data.sql` | ✅ 可以 | 同上。另外它**不需要 setval** —— `orders.id` 是 varchar 主键，没有自增序列 |

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
                    'trailer', 'container_trailer_record')
  and indexname like '%pattern%';
-- 期望 11 行:
--   idx_customer_name / idx_customer_qualification
--   idx_port_enname_pattern / idx_port_cnname_pattern
--   idx_cargo_type_name_pattern
--   idx_company_name_pattern / idx_company_code_pattern
--   idx_trailer_no_pattern / idx_trailer_name_pattern
--   idx_ctr_trailer_record_container_no_pattern / idx_ctr_trailer_record_track_no_pattern
```

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
```

再启动应用，打开 `http://localhost:8080/swagger-ui/index.html`，调一下 `GET /customer-statuses/all` 能返回 3 条就说明前 4 步都到位了。

## 八、几个容易踩的点

1. **顺序不能乱**，尤其 `customer-data.sql` 必须在 `customer-status-data.sql` 之后（见第三节）。
2. **`initial.sql` 只能跑一次**，其他几个可以反复跑。
3. **不要手工改内置状态的 id**。1-3（客户）和 1-4（订单）被 Java 代码写死依赖（`CustomerStatusConstants` / `OrderStatusConstants`），其中「注销」「已取消」还是逻辑删除的落点。这些状态在当前版本里**既不能改也不能删**（接口会返回 409）。
4. **`insert_time` / `update_time` 不要手工填**，`initial.sql` 里的触发器会自动维护，应用层也一律留空。
