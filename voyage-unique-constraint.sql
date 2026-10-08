-- =====================================================================
--  航次复合唯一约束 (vsl_id, no) —— **老库升级脚本**
--
--  什么时候需要跑这个文件:
--    你的数据库是**在本次改动之前**建的(也就是 initial.sql 里 Voyage 表还没有
--    uq_voyage_vsl_no 这条约束)。新库直接跑最新的 initial.sql 就已经有约束了, 不用跑这个。
--
--  背景:
--    航次号 no 之前完全不做唯一性校验, 于是"同一条船 + 同一个航次号"可以建出好多条 ——
--    这种重复在界面上完全分不出来(物流事件视图里显示的就是 船名 + 航次号), 轨迹会静默
--    挂到错的航次上。
--    但也不能给 no 加全库唯一: 航次号是**船公司自编**的, 不同公司、不同年份大量重号
--    (比如 001E 每年复用一条)。所以唯一的是 **(船, 航次号) 这一对**。
--
--  为什么可以在 no 仍然可空的情况下加约束:
--    PostgreSQL 的唯一约束**不比较 NULL**, 所以 (null, '2026E001') 这种行可以有任意多条,
--    彼此不冲突。正好对应应用层"船或航次号任一为空就跳过查重"的口径。
--
--  可重复执行: 用 do 块先查 pg_constraint, 已经加过就跳过。
--
--  ⚠️ 如果加约束时报 unique_violation, 说明库里**已经有**同船同号的重复航次 ——
--     先用下面这句把它们找出来, 决定留哪条、改哪条的航次号或船舶, 然后再重跑本文件:
--
--         select vsl_id, no, count(*), array_agg(id order by id) as ids
--         from voyage
--         where vsl_id is not null and no is not null
--         group by vsl_id, no
--         having count(*) > 1;
-- =====================================================================

begin;

do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'uq_voyage_vsl_no'
    ) then
        alter table Voyage
            add constraint uq_voyage_vsl_no unique (vsl_id, no);
    end if;
end $$;

comment on constraint uq_voyage_vsl_no on Voyage
    is '同一艘船的航次号不能重复(不同船可以重号)';

commit;
