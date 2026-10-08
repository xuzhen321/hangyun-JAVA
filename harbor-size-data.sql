-- =====================================================================
--  港口尺寸字典 初始数据 (PostgreSQL)
--
--  执行前提: 已执行 initial.sql。本表不依赖其他表。被 port.harbor_size_id 引用。
--
--  📌 取值沿用 UN/LOCODE 的港口规模档(Very Small → Very Large)。
--     字段没有唯一约束。
--
--  编写约定: insert_time / update_time 留 null 交给触发器; id 显式指定 +
--            on conflict do nothing 保证可重复执行; 末尾同步自增序列。
-- =====================================================================

begin;

insert into harbor_size (id, size) values
    (1, 'Very Small'),
    (2, 'Small'),
    (3, 'Medium'),
    (4, 'Large'),
    (5, 'Very Large')
on conflict do nothing;

-- 显式插入 identity 列不会移动序列, 这里手动同步。
select setval(
    pg_get_serial_sequence('harbor_size', 'id'),
    coalesce((select max(id) from harbor_size), 0) + 1,
    false
);

commit;
