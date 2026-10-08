-- =====================================================================
--  港口级别字典 初始数据 (PostgreSQL)
--
--  执行前提: 已执行 initial.sql。本表不依赖其他表。被 port.level_id 引用。
--  字段没有唯一约束。level 是数字。
--
--  📌 **级别按年集装箱吞吐量分档**(真实港口的分级习惯), level 越小级别越高:
--
--      level = 1  特大型港口(世界级枢纽)     年吞吐量 >= 3500 万 TEU
--      level = 2  大型港口(国际枢纽)         2000 ~ 3500 万
--      level = 3  中型港口(区域枢纽)         800 ~ 2000 万
--      level = 4  小型港口(地区性)           < 800 万
--
--    各港口的 level_id 见 port-data.sql, 例如上海港(4990万TEU)=1、连云港港(305万)=4。
--    阈值是**约值**, 按公开吞吐量数据粗分, 只为让这一栏有业务含义, 不必当作权威口径。
--
--  ⚠️ **如果你之前已经跑过本文件**: id 1/2/3 已存在(level 分别是 1/2/3, 和新口径一致,
--     不用改), 但 **id=4 是新加的**, 重跑本文件即可补上(insert 会插新行)。
--
--  编写约定: insert_time / update_time 留 null 交给触发器; id 显式指定 +
--            on conflict do nothing 保证可重复执行; 末尾同步自增序列。
-- =====================================================================

begin;

insert into port_level (id, level) values
    (1, 1),   -- 特大型港口: 世界级枢纽
    (2, 2),   -- 大型港口:   国际枢纽
    (3, 3),   -- 中型港口:   区域枢纽
    (4, 4)    -- 小型港口:   地区性
on conflict do nothing;

-- 显式插入 identity 列不会移动序列, 这里手动同步。
select setval(
    pg_get_serial_sequence('port_level', 'id'),
    coalesce((select max(id) from port_level), 0) + 1,
    false
);

commit;
