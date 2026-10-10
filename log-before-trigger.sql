-- =====================================================================
--  操作日志"改前值"(before_value) 触发器 (PostgreSQL)
--
--  执行前提: 已执行 initial.sql (log 表与 operation_type 字典都要在)。
--  本文件只新增一个函数 + 若干触发器, **不改动任何表结构**, 可重复执行。
--
--  为什么需要它:
--    LogAspect 切面手里只有方法签名和入参, 拿不到"改之前"的整行快照 ——
--    before_value 恰恰是审计里最有价值的一列(能看出到底把哪个字段从什么改成了什么)。
--    要在应用层补, 得在 27 个 Service 里逐个方法手工回查一次库, 重复代码太多;
--    交给数据库触发器写一次, 覆盖所有表。
--
--  ⚠️⚠️ 与 LogAspect 的关系 —— 启用前必须先想清楚 ⚠️⚠️
--    触发器和切面是**两套互相独立的记账方式**, 同一个 UPDATE/DELETE 会被记两遍。
--    正确用法是二选一:
--
--        UPDATE / DELETE  → 交给本文件的触发器 (它能拿到 OLD 整行)
--        INSERT / EXPORT / LOGIN → 仍然交给 LogAspect 切面
--
--    所以触发器**只挂 update / delete, 不挂 insert** —— INSERT 根本没有"改前值",
--    触发器记它只是把切面已经记过的东西再抄一遍, 只会让"新增"也变成两条日志。
--
--    **与之配套的代码改动已经做完**: 26 个 ServiceImpl 里 UPDATE / DELETE 方法上的
--    @OpLog(共 60 处)已全部去掉, 触发器是这些操作的唯一记录方。
--    唯一的例外是 Users 表 —— 它没挂触发器, 那 4 处 @OpLog 保留着, 继续走切面。
--
--  ⚠️ 代价(必须接受): 触发器在业务事务内, 业务回滚则日志也回滚 ——
--     失败的操作(比如校验没过抛异常)不会留下任何记录。
--     而应用层切面用 REQUIRES_NEW 独立事务, 失败也记。
--     这是"能拿到改前值"换来的代价: 两者不可兼得。
--     若某张表的失败留痕比改前值更重要, 就别给它挂这个触发器, 继续用 @OpLog。
--
--  ⚠️ user_id 从哪来: 触发器读 current_setting('app.user_id'), 由应用层的
--     com.test.hangyun.log.AuditUserInterceptor 在每个事务里先
--     set_config('app.user_id', <id>, true) 写进去。没接线时 user_id 记为 null。
--
--  ⚠️ Users 表**故意不挂**: password 列存的是 BCrypt 哈希, before/after 快照
--     会把哈希原样抄进 log 表。应用层切面靠 DTO 上的 @JsonProperty(WRITE_ONLY)
--     挡住了密码, 而数据库触发器是整行 to_jsonb, 根本不看那套注解。
--     Users 的日志继续由 @OpLog 切面负责。
-- =====================================================================

create or replace function log_row_change() returns trigger as $$
declare
v_pk_col  text;
    v_pk_val  text;
    v_type_id bigint;
    v_user_id bigint;
    v_before  text;
    v_after   text;
begin
    -- 主键列名不统一: 多数表叫 id, 但 Container / Trailer 叫 no。
    -- 不从 TG_ARGV 传参、也不硬编码, 直接从系统目录现取本表的主键列名。
select a.attname into v_pk_col
from pg_index i
         join pg_attribute a
              on a.attrelid = i.indrelid
                  and a.attnum = any (i.indkey)
where i.indrelid = tg_relid
  and i.indisprimary;

if tg_op = 'UPDATE' then
        -- 业务字段一个都没变就不记。只比对剔除 insert_time / update_time 之后的部分,
        -- 否则 set_time_fields() 刷新 update_time 会被误判成"有变化"。
        if (to_jsonb(new) - 'insert_time' - 'update_time')
           is not distinct from
           (to_jsonb(old) - 'insert_time' - 'update_time') then
            return null;                                 -- AFTER 触发器, 返回值被忽略
end if;

        v_type_id := 2;                                  -- UPDATE
        v_before  := to_jsonb(old)::text;
        v_after   := to_jsonb(new)::text;
        if v_pk_col is not null then
            v_pk_val := to_jsonb(old) ->> v_pk_col;
end if;

else                                                 -- DELETE
        v_type_id := 3;                                  -- DELETE
        v_before  := to_jsonb(old)::text;
        v_after   := null;
        if v_pk_col is not null then
            v_pk_val := to_jsonb(old) ->> v_pk_col;
end if;
end if;

    -- 当前操作人: 由应用层在每个事务内 set_config('app.user_id', <id>, true) 写入。
    -- 第二个参数 true = 键不存在时返回 null 而不是报错; 未接线时为 null。
    v_user_id := nullif(current_setting('app.user_id', true), '')::bigint;

insert into log (user_id, type_id, operation_time, result_status,
                 target_table, target_id, before_value, after_value)
values (
           v_user_id,
           v_type_id,
           now() at time zone 'Asia/Shanghai',   -- 与 set_time_fields() 保持同一时区约定
           '1',                                  -- 能走到这里说明语句没抛异常
           lower(tg_table_name),                 -- 对齐应用层写的 target_table 小写形式
           left(v_pk_val, 50),                   -- target_id 列是 varchar(50)
           left(v_before, 4000),                 -- 对齐 OperationLogConstants.MAX_VALUE_LENGTH
           left(v_after,  4000)
       );

-- insert_time / update_time 留空, 由 Log 表自己的 trg_set_time_log 填。
return null;
end;
$$ language plpgsql;


-- ---------------------------------------------------------------------
--  挂载。
--  ⚠️ 挂了触发器的表, 对应的 Service 写方法上的 @OpLog 已经去掉了 ——
--     否则 UPDATE/DELETE 会被切面和触发器各记一遍。写新代码时别再标回去。
--  ⚠️ 故意不挂 Log 表(会自己记自己, 无限递归) 和 Users 表(见文件头说明)。
--     这两张表的日志仍然由 LogAspect 切面负责。
-- ---------------------------------------------------------------------

-- 幂等: 先逐个摘掉已存在的同名触发器, 这样本文件可以反复执行
-- —— 包括从"挂 insert 的旧版本"升级上来时, 否则 create trigger 会报 already exists。
--
-- ⚠️ 这里刻意写成 **26 行显式 drop**, 而不是用 do 块循环查 information_schema:
--    information_schema.triggers 是**一个触发器 × 每个事件各一行** ——
--    旧版挂在 insert+update+delete 上, 同一个触发器会返回 3 行,
--    循环里被 drop 三次, 第二次就报 'trigger ... does not exist' 中断整个脚本。
--    显式列出来零花活, 还能和下面的 create 逐行对着核对。
drop trigger if exists trg_log_change_customer                 on Customer;
drop trigger if exists trg_log_change_customer_status          on Customer_Status;
drop trigger if exists trg_log_change_orders                   on Orders;
drop trigger if exists trg_log_change_order_status             on Order_Status;
drop trigger if exists trg_log_change_cargo_type               on Cargo_Type;
drop trigger if exists trg_log_change_cargo                    on Cargo;
drop trigger if exists trg_log_change_cargo_container_result   on Cargo_Container_Result;
drop trigger if exists trg_log_change_container                on Container;
drop trigger if exists trg_log_change_container_trailer_record on Container_Trailer_Record;
drop trigger if exists trg_log_change_container_type           on Container_Type;
drop trigger if exists trg_log_change_container_status         on Container_Status;
drop trigger if exists trg_log_change_company                  on Company;
drop trigger if exists trg_log_change_trailer                  on Trailer;
drop trigger if exists trg_log_change_container_event          on Container_Event;
drop trigger if exists trg_log_change_voyage                   on Voyage;
drop trigger if exists trg_log_change_event_status             on Event_Status;
drop trigger if exists trg_log_change_vessel                   on Vessel;
drop trigger if exists trg_log_change_ship_type                on Ship_Type;
drop trigger if exists trg_log_change_port                     on Port;
drop trigger if exists trg_log_change_country                  on Country;
drop trigger if exists trg_log_change_area                     on Area;
drop trigger if exists trg_log_change_timezone                 on Timezone;
drop trigger if exists trg_log_change_harbor_size              on Harbor_Size;
drop trigger if exists trg_log_change_port_level               on Port_Level;
drop trigger if exists trg_log_change_port_type                on Port_Type;
drop trigger if exists trg_log_change_operation_type           on Operation_Type;

create trigger trg_log_change_customer                 after update or delete on Customer                 for each row execute function log_row_change();
create trigger trg_log_change_customer_status          after update or delete on Customer_Status          for each row execute function log_row_change();
create trigger trg_log_change_orders                   after update or delete on Orders                   for each row execute function log_row_change();
create trigger trg_log_change_order_status             after update or delete on Order_Status             for each row execute function log_row_change();
create trigger trg_log_change_cargo_type               after update or delete on Cargo_Type               for each row execute function log_row_change();
create trigger trg_log_change_cargo                    after update or delete on Cargo                    for each row execute function log_row_change();
create trigger trg_log_change_cargo_container_result   after update or delete on Cargo_Container_Result   for each row execute function log_row_change();
create trigger trg_log_change_container                after update or delete on Container                for each row execute function log_row_change();
create trigger trg_log_change_container_trailer_record after update or delete on Container_Trailer_Record for each row execute function log_row_change();
create trigger trg_log_change_container_type           after update or delete on Container_Type           for each row execute function log_row_change();
create trigger trg_log_change_container_status         after update or delete on Container_Status         for each row execute function log_row_change();
create trigger trg_log_change_company                  after update or delete on Company                  for each row execute function log_row_change();
create trigger trg_log_change_trailer                  after update or delete on Trailer                  for each row execute function log_row_change();
create trigger trg_log_change_container_event          after update or delete on Container_Event          for each row execute function log_row_change();
create trigger trg_log_change_voyage                   after update or delete on Voyage                   for each row execute function log_row_change();
create trigger trg_log_change_event_status             after update or delete on Event_Status             for each row execute function log_row_change();
create trigger trg_log_change_vessel                   after update or delete on Vessel                   for each row execute function log_row_change();
create trigger trg_log_change_ship_type                after update or delete on Ship_Type                for each row execute function log_row_change();
create trigger trg_log_change_port                     after update or delete on Port                     for each row execute function log_row_change();
create trigger trg_log_change_country                  after update or delete on Country                  for each row execute function log_row_change();
create trigger trg_log_change_area                     after update or delete on Area                     for each row execute function log_row_change();
create trigger trg_log_change_timezone                 after update or delete on Timezone                 for each row execute function log_row_change();
create trigger trg_log_change_harbor_size              after update or delete on Harbor_Size              for each row execute function log_row_change();
create trigger trg_log_change_port_level               after update or delete on Port_Level               for each row execute function log_row_change();
create trigger trg_log_change_port_type                after update or delete on Port_Type                for each row execute function log_row_change();
create trigger trg_log_change_operation_type           after update or delete on Operation_Type           for each row execute function log_row_change();


-- ---------------------------------------------------------------------
--  接线: 让触发器能拿到 user_id  —— 已完成, 不需要再做什么
--
--  触发器跑在数据库里, 它不知道 HTTP 请求是谁发的, 所以要在同一个事务里
--  用 set_config 把当前用户写进"事务局部变量", 触发器再读出来:
--
--      select set_config('app.user_id', '7', true);
--
--  这一句已经由 **com.test.hangyun.log.AuditUserInterceptor** 代劳了:
--  它拦 MyBatis 的 Executor.update(只在写语句时触发), 在事务内、同一条连接上
--  先把 UserContext.currentUserId() 写进去, 再放行 SQL。
--
--  ⚠️ 第三个参数必须是 true(事务级)。用 false 是会话级的, Tomcat 线程池复用连接时
--     会把上一个请求的用户留给下一个请求 —— 和 UserContext 那个坑一模一样。
--
--  ⚠️ 因此写方法必须带 @Transactional, 否则那条隐式事务一结束, set_config 就失效。
--     项目里所有写方法都有, 新增写方法时别忘了。
--
--  ⚠️ 没接线时的表现: 日志照记, 只是 user_id 全是 null。不会报错。
-- ---------------------------------------------------------------------


-- ---------------------------------------------------------------------
--  启用后自查(手工执行, 不在本文件里跑):
--
--  -- 1) 触发器是否都挂上了
--  select event_object_table, trigger_name
--    from information_schema.triggers
--   where trigger_name like 'trg_log_change%'
--   order by 1;
--
--  -- 2) 最近几条日志的改前/改后
--  select id, target_table, target_id, type_id,
--         before_value, after_value
--    from log
--   where before_value is not null
--   order by id desc
--   limit 10;
--
--  回滚(把触发器全摘掉, 函数可留可删):
--  do $$
--  declare r record;
--  begin
--      for r in select t.tgname as tg_name, c.relname as tbl_name
--                 from pg_trigger t
--                 join pg_class     c on c.oid = t.tgrelid
--                 join pg_namespace n on n.oid = c.relnamespace
--                where not t.tgisinternal
--                  and t.tgname like 'trg_log_change%'
--                  and n.nspname = current_schema()
--      loop
--          execute format('drop trigger if exists %I on %I.%I',
--                         r.tg_name, current_schema(), r.tbl_name);
--      end loop;
--  end $$;
--  -- drop function log_row_change();
-- ---------------------------------------------------------------------
