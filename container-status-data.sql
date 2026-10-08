-- =====================================================================
--  集装箱状态字典 初始数据 (PostgreSQL)
--
--  执行前提: 已执行 initial.sql —— container_status 表和 set_time_fields() 触发器都已存在。
--
--  ⚠️ 注意这张表的结构和其他状态字典**不一样**: 它有两列描述(中英文各一列),
--     客户状态 / 订单状态那种只有一列 description。两列在库里都有唯一约束。
--
--  ⚠️ /container-statuses 这个**完整资源还没做**(没有增删改接口), 本文件只是把
--     字典数据备好, 让新增集装箱时"当前状态"那个下拉框有东西可选。
--
--  编写约定: insert_time / update_time 留 null 交给触发器; id 显式指定 +
--            on conflict do nothing 保证可重复执行; 末尾同步自增序列。
-- =====================================================================

begin;

insert into container_status (id, description_cn, description_en) values
    (1, '在途',   'In Transit'),
    (2, '在堆场', 'At Yard'),
    (3, '已装船', 'Loaded'),
    (4, '已卸船', 'Discharged'),
    (5, '待提箱', 'Awaiting Pickup'),
    (6, '已提箱', 'Picked Up')
on conflict do nothing;

-- 显式插入 identity 列不会移动序列, 这里手动同步。
select setval(
    pg_get_serial_sequence('container_status', 'id'),
    coalesce((select max(id) from container_status), 0) + 1,
    false
);

commit;
