-- =====================================================================
--  装箱结果表 验证数据 (PostgreSQL)
--
--  ⚠️ 这**不是**业务种子数据 —— 装箱模块还没做, 没有对应的接口和页面。
--     它的唯一用途是: 让「订单货物的删除保护」这条路径**可以被验证**。
--
--  背景: 删除货物是物理删除, 删除前会检查 cargo_container_result 有没有引用它。
--        但那张表本来是空的, 所以删任何货物都会成功 —— 这道保护等于测不到。
--        插入下面几行之后, 被引用的货物就删不掉了。
--
--  执行前提:
--    1. initial.sql    —— cargo_container_result 和 set_time_fields() 触发器已存在
--    2. cargo-data.sql —— 货物 1 / 3 已存在(下面要引用它们)
--
--  数据安排:
--    · cargo_id = 1 被**两条**装箱记录引用  ← 故意重复, 用来验证后端数的是
--      "几条货物"而不是"几条装箱记录"(count(distinct cargo_id))。
--      正确结果是报"1 条", 如果报"2 条"说明用错了 count(*)。
--    · cargo_id = 3 被一条装箱记录引用
--    · 其余货物(2、4、5...)没有被引用, 用来验证"能删"的那条路
--
--  ⚠️ container_no 是编的 —— container 表也是空的(集装箱模块没做), 属于悬空引用。
--     这里不影响验证, 因为删除保护只看 cargo_id。
--
--  验证清单:
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
    (3, 3, 'TGHU9876543', 100)
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
