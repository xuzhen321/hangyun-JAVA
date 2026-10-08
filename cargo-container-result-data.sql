-- =====================================================================
--  货物装箱结果表 模拟数据 (PostgreSQL)
--
--  两个用途:
--    1. 让 /cargo-container-results 的列表、筛选、增删改有东西可试;
--    2. 让「订单货物的删除保护」可以被验证 —— 删货物是物理删除, 删除前会检查
--       cargo_container_result 有没有引用它。表空着的话删任何货物都会成功, 这道保护
--       就等于测不到。
--
--  执行前提:
--    1. initial.sql    —— cargo_container_result 和 set_time_fields() 触发器已存在
--    2. cargo-data.sql —— 货物 1 / 3 已存在(下面要引用它们)
--    3. container-data.sql —— 箱号 CSNU1234567 / CSNU7654321 / TGHU9876543 已存在
--
--  数据安排:
--    · cargo_id = 1 被**两条**装箱记录引用  ← 故意重复, 用来验证后端数的是
--      "几条货物"而不是"几条装箱记录"(count(distinct cargo_id))。
--      正确结果是报"1 条", 如果报"2 条"说明用错了 count(*)。
--    · cargo_id = 3 被一条装箱记录引用
--    · 其余货物(2、4、5...)没有被引用, 用来验证"能删"的那条路
--    · 货物 1 属于订单 ...001、货物 3 属于订单 ...002, 所以按订单号筛时
--      orderId=1783100000000000001 应命中 2 条、...002 命中 1 条
--
--  ⚠️ 装箱量必须满足「同一 cargo_id 的装箱量之和 ≤ 该货物的 quantity」
--     (cargo-data.sql 里货物 1 = 120 件、货物 3 = 60 件), 否则会被接口的业务校验拒绝。
--     这里的安排是:
--       货物 1: 40 + 40 = 80 件, 共 120 件 → **还剩 40 件可装**
--       货物 3: 50 件,            共  60 件 → **还剩 10 件可装**
--     所以可以拿这两个剩余量去验证:
--       · 给货物 1 装 40 件 → 成功; 再装 1 件 → 409「最多还能装 0 件」
--       · 给货物 3 装 11 件 → 409「最多还能装 10 件」
--
--  ⚠️ 执行顺序: 本文件的箱号引用了 container-data.sql 里的集装箱,
--     所以要在它**之后**执行(否则接口层的"箱号存在性校验"会拦住新建, 见下)。
--
--  ⚠️ 箱号后 7 位是编的, 没有按真实的校验位算法计算(同 container-data.sql)。
--
--  验证清单(装箱结果接口本身):
--    GET  /cargo-container-results?cargoTypeName=钢   → 货物 1/3 都是"钢材", 命中 3 条
--    GET  /cargo-container-results?containerNo=CSNU1234567 → 1 条(精确匹配)
--    GET  /cargo-container-results?orderId=1783100000000000001 → 2 条
--
--  验证清单(货物删除保护):
--    DELETE /cargos/1                 → 409  该货物已被装箱记录引用, 无法删除
--    DELETE /cargos/3                 → 409
--    DELETE /cargos/2                 → 200  成功(没被引用)
--    DELETE /cargos/batch [2, 5]      → 200  成功
--    DELETE /cargos/batch [1, 2]      → 409  整批拒绝(1 被引用), 2 也不会被删掉
--
--  编写约定: insert_time / update_time 留 null 交给触发器; id 显式指定 +
--            on conflict do nothing 保证可重复执行; 末尾同步自增序列。
-- =====================================================================

begin;

insert into cargo_container_result (id, cargo_id, container_no, quantity) values
    (1, 1, 'CSNU1234567', 40),
    (2, 1, 'CSNU7654321', 40),
    (3, 3, 'TGHU9876543', 50)
on conflict do nothing;

-- 显式插入 identity 列不会移动序列, 这里手动同步。
select setval(
    pg_get_serial_sequence('cargo_container_result', 'id'),
    coalesce((select max(id) from cargo_container_result), 0) + 1,
    false
);

commit;

-- =====================================================================
--  验证完之后怎么撤销?
--  下面这条会删掉本文件插入的数据, 让货物恢复到"都能删"的状态:
--
--      delete from cargo_container_result where id in (1, 2, 3);
--
--  (或者干脆留着 —— 它不影响其他功能, 只是让货物 1 和 3 删不掉。)
-- =====================================================================
