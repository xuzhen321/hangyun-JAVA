-- =====================================================================
--  时区字典 初始数据 (PostgreSQL)
--
--  执行前提: 已执行 initial.sql。本表不依赖其他表。被 port.timezone_id 引用。
--
--  ⚠️ 两个字段名都是**带双引号的大小写混合列名**, 不加引号会报"列不存在":
--       "timezone_UTC8"          港口时区
--       "timezone_Asia_Shanghai" 港口时区(第二种写法)
--
--  ⚠️ **两列在库里都有唯一约束**, 所以:
--     · 每个 UTC 偏移只能出现一次 —— 上海和新加坡都是 UTC+8, 但只能建其中一条
--       (新加坡那条港口用的就是"Asia/Shanghai"这条时区)
--     · 每条记录的两个值都必须互不相同
--
--  编写约定: insert_time / update_time 留 null 交给触发器; id 显式指定 +
--            on conflict do nothing 保证可重复执行; 末尾同步自增序列。
-- =====================================================================

begin;

insert into timezone (id, "timezone_UTC8", "timezone_Asia_Shanghai") values
    (1, 'UTC+08:00', 'Asia/Shanghai'),
    (2, 'UTC-08:00', 'America/Los_Angeles'),
    (3, 'UTC+09:00', 'Asia/Tokyo'),
    (4, 'UTC+00:00', 'Europe/London'),
    (5, 'UTC+01:00', 'Europe/Berlin'),
    (6, 'UTC+10:00', 'Australia/Sydney')
on conflict do nothing;

-- 显式插入 identity 列不会移动序列, 这里手动同步。
select setval(
    pg_get_serial_sequence('timezone', 'id'),
    coalesce((select max(id) from timezone), 0) + 1,
    false
);

commit;
