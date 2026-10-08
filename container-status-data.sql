-- =====================================================================
--  集装箱状态字典 初始数据 (PostgreSQL)
--
--  执行前提: 已执行 initial.sql —— container_status 表和 set_time_fields() 触发器都已存在。
--
--  ⚠️ 注意这张表的结构和其他状态字典**不一样**: 它有两列描述(中英文各一列),
--     客户状态 / 订单状态那种只有一列 description。两列在库里都有唯一约束。
--
--  ⚠️ **id=7「已删除」是内置的, 不能改也不能删** —— 集装箱的"删除"就是把它改成这个状态
--     (逻辑删除, 这样装箱结果等历史记录还能查到)。代码里 ContainerStatusConstants
--     写死了这个 id, 和本文件必须一致。
--     其余 1-6 都是普通字典项, 只要没被集装箱引用就能随便改、随便删。
--
--  📌 1-6 这六条是**为联调编的示例值**(常见的集装箱状态), 周报里没有定义过这张表的取值。
--     如果之后有了官方列表, 直接改这里即可。
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
    (6, '已提箱', 'Picked Up'),
    (7, '已删除', 'Deleted')
on conflict do nothing;

-- 显式插入 identity 列不会移动序列, 这里手动同步。
select setval(
    pg_get_serial_sequence('container_status', 'id'),
    coalesce((select max(id) from container_status), 0) + 1,
    false
);

commit;
