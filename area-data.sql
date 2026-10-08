-- =====================================================================
--  区域字典 初始数据 (PostgreSQL)
--
--  执行前提: 已执行 initial.sql。本表不依赖其他表。
--  被 port.area_id 引用。area_name 在库里有唯一约束。
--
--  编写约定: insert_time / update_time 留 null 交给触发器; id 显式指定 +
--            on conflict do nothing 保证可重复执行; 末尾同步自增序列。
-- =====================================================================

begin;

insert into area (id, area_name) values
    (1, 'North China'),
    (2, 'East China'),
    (3, 'South China'),
    (4, 'Southeast Asia'),
    (5, 'North America'),
    (6, 'Europe')
on conflict do nothing;

-- 显式插入 identity 列不会移动序列, 这里手动同步。
select setval(
    pg_get_serial_sequence('area', 'id'),
    coalesce((select max(id) from area), 0) + 1,
    false
);

commit;
