-- =====================================================================
--  集装箱箱型字典 初始数据 (PostgreSQL)
--
--  执行前提: 已执行 initial.sql —— container_type 表和 set_time_fields() 触发器都已存在。
--            本表不引用其他表, 只依赖第 1 步, 顺序很自由。
--
--  6 种常见箱型。箱型由"类别 + 尺寸"两个字段组合而成, 接口上是分开返回的
--  (typeName / typeSize), 前端拼起来显示即可。
--
--  ⚠️ /container-types 这个**完整资源还没做**(没有增删改接口), 本文件只是把字典
--     数据备好, 让新增集装箱时那几个下拉框有东西可选。
--
--  编写约定: insert_time / update_time 留 null 交给触发器; id 显式指定 +
--            on conflict do nothing 保证可重复执行; 末尾同步自增序列。
-- =====================================================================

begin;

insert into container_type (id, type, size) values
    (1, '普通箱', '20英尺'),
    (2, '普通箱', '40英尺'),
    (3, '高箱',   '40英尺'),
    (4, '冷藏箱', '40英尺'),
    (5, '开顶箱', '20英尺'),
    (6, '框架箱', '40英尺')
on conflict do nothing;

-- 显式插入 identity 列不会移动序列, 这里手动同步。
select setval(
    pg_get_serial_sequence('container_type', 'id'),
    coalesce((select max(id) from container_type), 0) + 1,
    false
);

commit;
