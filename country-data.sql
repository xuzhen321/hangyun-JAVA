-- =====================================================================
--  国家字典 初始数据 (PostgreSQL)
--
--  执行前提: 已执行 initial.sql。本表不引用其他表, 只依赖第 1 步。
--
--  ⚠️ 代码用 ISO 3166-1 两字母码(CN/US/...)。country_code 在库里有唯一约束。
--     被 vessel.country_id(船旗国) 和 port.country_id(港口所在国家) 引用。
--
--  编写约定: insert_time / update_time 留 null 交给触发器; id 显式指定 +
--            on conflict do nothing 保证可重复执行; 末尾同步自增序列。
-- =====================================================================

begin;

insert into country (id, country_code, country_cnname, country_enname) values
    (1,  'CN', '中国',       'China'),
    (2,  'US', '美国',       'United States'),
    (3,  'JP', '日本',       'Japan'),
    (4,  'KR', '韩国',       'South Korea'),
    (5,  'SG', '新加坡',     'Singapore'),
    (6,  'DE', '德国',       'Germany'),
    (7,  'NL', '荷兰',       'Netherlands'),
    (8,  'GB', '英国',       'United Kingdom'),
    (9,  'AU', '澳大利亚',   'Australia'),
    (10, 'HK', '中国香港',   'Hong Kong')
on conflict do nothing;

-- 显式插入 identity 列不会移动序列, 这里手动同步。
select setval(
    pg_get_serial_sequence('country', 'id'),
    coalesce((select max(id) from country), 0) + 1,
    false
);

commit;
