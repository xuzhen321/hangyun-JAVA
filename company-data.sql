-- =====================================================================
--  公司信息表 初始数据 (PostgreSQL)
--
--  执行前提: 已执行 initial.sql —— company 表和 set_time_fields() 触发器都已存在。
--
--  ⚠️ 这是**多用途字典**: 箱主、操作方、船东、管理公司都用它一张表。
--     本文件里的公司主要是集装箱的箱主 / 操作方。
--
--  ⚠️ code 是**箱主代码**(BIC 代码), 也就是箱号的开头四位 ——
--     比如箱号 CSNU1234567 的箱主代码就是 CSNU。所以下面 7、8 两家是专门
--     为了让 container-data.sql 里那几个箱号能和箱主对得上才加的。
--
--  ⚠️ /companies 这个**完整资源还没做**(没有增删改接口), 本文件只是把字典数据备好。
--
--  编写约定: insert_time / update_time 留 null 交给触发器; id 显式指定 +
--            on conflict do nothing 保证可重复执行; 末尾同步自增序列。
-- =====================================================================

begin;

insert into company (id, name, code) values
    (1, '中远海运集装箱运输有限公司', 'COSU'),
    (2, '马士基航运',                 'MSKU'),
    (3, '地中海航运',                 'MSCU'),
    (4, '达飞轮船',                   'CMAU'),
    (5, '赫伯罗特',                   'HLXU'),
    (6, '长荣海运',                   'EGHU'),
    (7, '中海集装箱运输有限公司',     'CSNU'),
    (8, '泰腾箱务有限公司',           'TGHU')
on conflict do nothing;

-- 显式插入 identity 列不会移动序列, 这里手动同步。
select setval(
    pg_get_serial_sequence('company', 'id'),
    coalesce((select max(id) from company), 0) + 1,
    false
);

commit;
