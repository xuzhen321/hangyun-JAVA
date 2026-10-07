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

## 二、可选的 6 个

| 文件 | 作用 | 依赖 |
|---|---|---|
| `customer-data.sql` | 10 条客户样本数据 | 第 3 步（状态 1/2/3） |
| `port-data.sql` | 10 条港口模拟数据（含 UN/LOCODE 和中英文名） | 第 1 步 |
| `cargo-type-data.sql` | 12 条货物种类样本数据 | 第 1 步 |
| `order-data.sql` | 12 条订单样本数据 | **上面的客户 + 港口，以及第 4 步的状态** |
| `cargo-data.sql` | 14 条货物样本数据 | **货物种类 + 订单** |
| `cargo-container-result-data.sql` | **验证专用**，见下 | **`cargo-data.sql`** |

前五个都是**纯测试数据，不装不影响功能**，但装了才能端到端联调：

- 不装 `customer-data.sql` → 客户列表是空的，没法选客户下单
- 不装 `port-data.sql` → `GET /ports/options` 返回 `[]`，起运港/目的港选不了
- 不装 `cargo-type-data.sql` → 货物种类列表是空的，名称搜索没东西可试
- 不装 `order-data.sql` → 订单列表是空的，`GET /customers/{id}/orders` 也查不到东西
- 不装 `cargo-data.sql` → `GET /cargos` 查不到东西；**货物种类的删除保护也测不出来**（没有货物引用它们，删谁都会成功）

⚠️ **最后那个不一样，它是"验证专用"的**：往 `cargo_container_result`（装箱结果）里插几行，让货物能被引用，**从而验证"订单货物的删除保护"**。装箱模块本身还没做，所以它不是业务数据。不需要验证删除保护的话**可以不装**；装了之后货物 1 和 3 就删不掉了（文件末尾附了撤销语句）。

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
7. cargo-type-data.sql      货物种类样本（可选, 只依赖第 1 步）
        ↓
8. order-data.sql           订单样本（可选，依赖第 4/5/6 步）
        ↓
9. cargo-data.sql           货物样本（可选，依赖第 7/8 步）
        ↓
10. cargo-container-result-data.sql   装箱结果验证数据（可选，依赖第 9 步）
```

第 6、7 步只依赖第 1 步，放在第 5 步前后都行。

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
psql -h localhost -p 5432 -U postgres -d demo -f cargo-container-result-data.sql  # 可选, 只为验证删除保护
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
| `cargo-container-result-data.sql` | ✅ 可以 | 同上 |
| `order-data.sql` | ✅ 可以 | 同上。另外它**不需要 setval** —— `orders.id` 是 varchar 主键，没有自增序列 |

如果 `initial.sql` 跑到一半失败了，需要先把已建的表删掉再重跑，或者直接删库重建。

## 六、已经建过库了？增量变更

`initial.sql` 只能跑一次（见上一节），所以**库已经建过的话，下面这些改动不会自动生效**，要手动执行。

### 6.1 Cargo 三列改为 not null

`cargo_type_id` / `order_id` / `quantity` 现在都是 `not null`（这三列是货物的必需信息）。

> ⚠️ **执行前先确认没有空值**，否则 `set not null` 会直接失败：
>
> ```sql
> select count(*) from cargo
> where cargo_type_id is null or order_id is null or quantity is null;
> ```
>
> 结果不是 0 的话，得先把这些行补全或删掉。如果你之前装过旧版的 `cargo-data.sql`，里面 id=13 那行 `cargo_type_id` 是空的，先执行：
>
> ```sql
> update cargo set cargo_type_id = 1 where cargo_type_id is null;
> ```

```sql
alter table cargo alter column cargo_type_id set not null;
alter table cargo alter column order_id      set not null;
alter table cargo alter column quantity      set not null;
```

### 6.2 索引的增量变更

#### 港口的中英文名前缀索引（给 `/ports/options` 用）

```sql
create index idx_port_enname_pattern on Port (enname varchar_pattern_ops);
create index idx_port_cnname_pattern on Port (cnname varchar_pattern_ops);
```

#### 货物种类的名称前缀索引（给 `/cargo-types` 的 name 搜索用）

```sql
create index idx_cargo_type_name_pattern on Cargo_Type (name varchar_pattern_ops);
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
where tablename in ('customer', 'port', 'cargo_type') and indexname like '%pattern%';
-- 期望 5 行:
--   idx_customer_name / idx_customer_qualification
--   idx_port_enname_pattern / idx_port_cnname_pattern
--   idx_cargo_type_name_pattern
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
```

再启动应用，打开 `http://localhost:8080/swagger-ui/index.html`，调一下 `GET /customer-statuses/all` 能返回 3 条就说明前 4 步都到位了。

## 八、几个容易踩的点

1. **顺序不能乱**，尤其 `customer-data.sql` 必须在 `customer-status-data.sql` 之后（见第三节）。
2. **`initial.sql` 只能跑一次**，其他几个可以反复跑。
3. **不要手工改内置状态的 id**。1-3（客户）和 1-4（订单）被 Java 代码写死依赖（`CustomerStatusConstants` / `OrderStatusConstants`），其中「注销」「已取消」还是逻辑删除的落点。这些状态在当前版本里**既不能改也不能删**（接口会返回 409）。
4. **`insert_time` / `update_time` 不要手工填**，`initial.sql` 里的触发器会自动维护，应用层也一律留空。
