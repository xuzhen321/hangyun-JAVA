-- =====================================================================
--  集装箱信息表 初始数据 (PostgreSQL)
--
--  执行前提(缺一不可, 因为集装箱要引用这三张字典):
--    1. initial.sql             —— container 表和 set_time_fields() 触发器已存在
--    2. container-type-data.sql —— 箱型 1-6
--    3. container-status-data.sql —— 状态 1-6
--    4. company-data.sql        —— 公司 1-8
--
--  6 个集装箱。**前三个的箱号是特意选的** —— 它们正是
--  cargo-container-result-data.sql 里那 3 条装箱记录引用的箱号:
--    CSNU1234567 / CSNU7654321 / TGHU9876543
--  这样两边就对得上了: 装箱记录引用的箱号在 container 表里真实存在,
--  箱号存在性校验(见 CargoContainerResultServiceImpl)也就能通过。
--
--  箱号前缀与箱主代码的对应关系(前四位是箱主代码):
--    CSNU... -> company 7 (中海, CSNU)
--    TGHU... -> company 8 (泰腾, TGHU)
--    MSKU/MSCU/CMAU... -> 对应的公司
--
--  ⚠️ 箱号后 7 位(6 位序号 + 1 位校验位)是**编的**, 没有按真实的校验位算法计算。
--     不影响功能, 但不要拿去做真实业务。
--
--  两个标志位: '0' 否, '1' 是。cargo_operate / ctr_status_terminal 可以留空。
--
--  编写约定:
--    1. insert_time / update_time 不写值, 留 null 交给 set_time_fields() 触发器。
--    2. 主键是箱号(varchar), 没有自增序列, 所以**不需要 setval**(和 orders 一样)。
--    3. on conflict do nothing 保证脚本可以重复执行。
-- =====================================================================

begin;

insert into container (no, type_id, owner_id, operator_id, seal_no, status_id,
                       danger_flag, maritime_flag, carrier_operate, ctr_status_terminal) values
    ('CSNU1234567', 2, 7, 7, 'SL2026001', 2, '0', '0', null,   '在场'),
    ('CSNU7654321', 3, 7, 7, 'SL2026002', 3, '0', '1', null,   null),
    ('TGHU9876543', 1, 8, 1, null,        1, '1', '0', '加锁', null),
    ('MSKU1234565', 4, 2, 2, 'SL2026004', 2, '0', '0', null,   '在场'),
    ('MSCU2345671', 1, 3, 3, null,        5, '0', '0', null,   null),
    ('CMAU3456783', 2, 4, 4, 'SL2026006', 6, '0', '0', null,   '出场')
on conflict do nothing;

commit;
-- 注意: container 的主键是 varchar, 没有自增序列, 所以这里没有 setval。
