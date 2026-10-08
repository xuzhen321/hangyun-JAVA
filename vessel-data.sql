-- =====================================================================
--  船舶信息表 模拟数据 (PostgreSQL)
--
--  执行前提:
--    1. initial.sql      —— vessel 表和 set_time_fields() 触发器已存在
--    2. company-data.sql —— 下面引用的船东/管理公司(1-6)已存在
--
--  ⚠️ 这个文件是**为了打通"船名"这条链**才造的:
--     集装箱轨迹里的船名要沿 事件 → 航次 → 船舶 两跳才拿得到, 而 vessel 表本来是空的。
--     装上它 + 把 voyage-data.sql 里的 vsl_id 填上, 轨迹接口就有船名了。
--     (船舶模块 /vessels 本身还没做, 这里只是数据。)
--
--  ⚠️ 两个字段留空:
--       vessel_type_id -> ship_type 表是空的
--       country_id     -> country 表是空的
--     填了就是悬空引用, 所以不填。等那两张字典有数据了再补。
--     owner_company_id / manager_company_id 用的是 company-data.sql 里真实存在的公司。
--
--  ⚠️ 船名、MMSI、IMO、呼号都是**编的**, IMO 也没有按真实校验位算法计算。
--     (mmsi / imo / callsign 在库里有唯一约束, 所以几条船之间不能重复。)
--
--  编写约定: insert_time / update_time 留 null 交给触发器; id 显式指定 +
--            on conflict do nothing 保证可重复执行; 末尾同步自增序列。
-- =====================================================================

begin;

insert into vessel (id, name, vessel_type_id, country_id, mmsi, imo, build_year,
                    owner_company_id, manager_company_id, callsign) values
    (1, '中远海运之星',    null, null, '413000001', '9780001', 2019, 1, 1, 'BQAB'),
    (2, '马士基哥本哈根',  null, null, '413000002', '9780002', 2020, 2, 2, 'BQAC'),
    (3, '地中海伊莎贝拉',  null, null, '413000003', '9780003', 2018, 3, 3, 'BQAD'),
    (4, '达飞雅克萨德',    null, null, '413000004', '9780004', 2021, 4, 4, 'BQAE'),
    (5, '长荣之星',        null, null, '413000005', '9780005', 2017, 6, 6, 'BQAF')
on conflict do nothing;

-- 显式插入 identity 列不会移动序列, 这里手动同步。
select setval(
    pg_get_serial_sequence('vessel', 'id'),
    coalesce((select max(id) from vessel), 0) + 1,
    false
);

commit;
