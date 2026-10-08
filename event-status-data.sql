-- =====================================================================
--  集装箱事件状态字典 初始数据 (PostgreSQL)
--
--  执行前提: 已执行 initial.sql —— event_status 表和 set_time_fields() 触发器都已存在。
--            本表不引用其他表, 只依赖第 1 步。
--
--  ⚠️ 这张表的结构和**集装箱状态**一样: 两列描述(中英文各一列), 两列都有唯一约束。
--
--  ⚠️ **只有 id=6「已删除」是内置的**, 不能改也不能删 —— 集装箱事件的"删除"就是把它改成
--     这个状态(逻辑删除, 这样物流轨迹等历史记录还查得到)。代码里 EventStatusConstants
--     写死了这个 id, 和本文件必须一致。
--     **1-5 是普通字典项**, 只要没被事件引用就能随便改、随便删。
--
--  📌 1-5 是按物流节点定的(周报里没有定义过这张表的取值)。
--
--  编写约定: insert_time / update_time 留 null 交给触发器; id 显式指定 +
--            on conflict do nothing 保证可重复执行; 末尾同步自增序列。
-- =====================================================================

begin;

insert into event_status (id, description_cn, description_en) values
    (1, '预安排', 'Scheduled'),
    (2, '装船中', 'Loading'),
    (3, '航行中', 'In Transit'),
    (4, '卸货中', 'Discharging'),
    (5, '已完成', 'Completed'),
    (6, '已删除', 'Deleted')
on conflict do nothing;

-- 显式插入 identity 列不会移动序列, 这里手动同步。
select setval(
    pg_get_serial_sequence('event_status', 'id'),
    coalesce((select max(id) from event_status), 0) + 1,
    false
);

commit;
