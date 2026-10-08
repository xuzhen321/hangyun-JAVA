-- =====================================================================
--  港口类型字典 初始数据 (PostgreSQL)
--
--  执行前提: 已执行 initial.sql。本表不依赖其他表。被 port.port_type_id 引用。
--  字段没有唯一约束。取值沿用建表脚本里 type 的列注释: 1系统规范港口, 2用户自定义港口。
--
--  编写约定: insert_time / update_time 留 null 交给触发器; id 显式指定 +
--            on conflict do nothing 保证可重复执行; 末尾同步自增序列。
-- =====================================================================

begin;

insert into port_type (id, type) values
    (1, '系统规范港口'),
    (2, '用户自定义港口')
on conflict do nothing;

-- 显式插入 identity 列不会移动序列, 这里手动同步。
select setval(
    pg_get_serial_sequence('port_type', 'id'),
    coalesce((select max(id) from port_type), 0) + 1,
    false
);

commit;
