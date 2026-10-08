-- =====================================================================
--  操作日志表 模拟数据 (PostgreSQL)
--
--  执行前提(缺一不可):
--    1. initial.sql               —— log 表和触发器已存在
--    2. user-data.sql             —— 操作人 1/2 已存在
--    3. operation-type-data.sql   —— 操作类型 1~5 (INSERT/UPDATE/DELETE/EXPORT/LOGIN) 已存在
--
--  8 条样本, 覆盖了列表和导出需要验证的情况:
--    · **两种结果**: id 6 的 result_status='0'(失败) —— 那条会带错误信息
--    · **五种类型**: INSERT / UPDATE / DELETE / EXPORT / LOGIN 各有样本
--    · **两个操作人**: admin(id=1) 和 zhangmin(id=2)
--
--  ⚠️ 正常运行时这张表**由日志切面自动写入**, 不需要手工插。
--     这个文件只是为了"刚装完系统就有东西可看", 以及让导出接口能验证。
--
--  ⚠️ 关于 id: Log.id 是**自增**(identity)。这里手工指定成 1~8 只是为了好认,
--     页面上后续产生的日志会接着从 9 往下排。
--
--  编写约定: insert_time / update_time 留 null 交给触发器; id 显式指定 +
--            on conflict do nothing 保证可重复执行; 末尾同步自增序列。
--  ⚠️ 显式插入 id **不会推进 identity 序列**, 不跑末尾的 setval 的话,
--     下一条由切面写入的日志会拿到 id=1, 撞已存在的行报 500 —— 而且是在
--     业务操作成功之后报, 表现成"操作成功了但接口返回 500", 很难查。
-- =====================================================================

begin;

insert into log (id, user_id, type_id, operation_time, result_status, error_message,
                 target_table, target_id, before_value, after_value) values
    (1, 1, 5, '2026-10-08 09:00:00', '1', null,
     'users', '1', null, null),
    (2, 1, 1, '2026-10-08 09:05:12', '1', null,
     'vessel', null, null, '[{"name":"中远海运之星","vesselTypeId":1,"countryId":1}]'),
    (3, 1, 2, '2026-10-08 09:12:40', '1', null,
     'port', '1', null, '[{"unlocode":"CNSHA","cnname":"上海港"}]'),
    (4, 2, 5, '2026-10-08 09:30:00', '1', null,
     'users', '2', null, null),
    (5, 2, 1, '2026-10-08 09:41:03', '1', null,
     'voyage', null, null, '[{"no":"2026E001","vslId":1,"loadingPortId":1}]'),
    (6, 2, 3, '2026-10-08 10:02:15', '0', '该航次下存在 3 条物流事件, 无法删除',
     'voyage', '1', null, null),
    (7, 1, 4, '2026-10-08 10:20:00', '1', null,
     'log', null, null, null),
    (8, 1, 2, '2026-10-08 10:35:26', '1', null,
     'vessel', '3', null, '[{"name":"地中海伊莎贝拉","vesselTypeId":1,"countryId":6}]')
on conflict do nothing;

-- 显式插入 identity 列不会移动序列, 这里手动同步。
-- setval(..., n, false) 表示下一次 nextval 返回 n, 所以取 max(id) + 1。
select setval(
    pg_get_serial_sequence('log', 'id'),
    coalesce((select max(id) from log), 0) + 1,
    false
);

commit;
