-- =====================================================================
--  航次信息表 模拟数据 (PostgreSQL)
--
--  执行前提: 已执行 initial.sql 和 port-data.sql（下面引用港口 1/2/3/9）。
--
--  4 个航次。两个港口都是 port-data.sql 里真实存在的, 所以"港口必须存在"那道校验能过。
--
--  📌 vsl_id 指向 vessel-data.sql 里的船(1-4), 装完这两个文件, 物流事件的**船名**就能显示了。
--
--  ⚠️ **如果你之前已经跑过本文件**, 那几条航次已经存在了 —— 下面的 insert 配 on conflict
--     不会更新它们, vsl_id 还是 null。**手工补一句**:
--
--         update voyage set vsl_id = 1 where id = 1;
--         update voyage set vsl_id = 2 where id = 2;
--         update voyage set vsl_id = 3 where id = 3;
--         update voyage set vsl_id = 4 where id = 4;
--
--  ⚠️ 航次号是编的。
--
--  编写约定: insert_time / update_time 留 null 交给触发器; id 显式指定 +
--            on conflict do nothing 保证可重复执行; 末尾同步自增序列。
-- =====================================================================

begin;

insert into voyage (id, no, vsl_id, loading_port_id, discharge_port_id) values
    (1, '2026E001', 1, 1, 9),   -- 上海 → 洛杉矶（中远海运之星）
    (2, '2026E002', 2, 3, 2),   -- 深圳 → 宁波（马士基哥本哈根）
    (3, '2026W011', 3, 2, 3),   -- 宁波 → 深圳（地中海伊莎贝拉）
    (4, '2026E003', 4, 1, 2)    -- 上海 → 宁波（达飞雅克萨德）
on conflict do nothing;

-- 显式插入 identity 列不会移动序列, 这里手动同步。
select setval(
    pg_get_serial_sequence('voyage', 'id'),
    coalesce((select max(id) from voyage), 0) + 1,
    false
);

commit;
