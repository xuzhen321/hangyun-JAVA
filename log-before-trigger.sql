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
--        INSERT / EXPORT / LOGIN → 仍然交给 LogAspect 切面 (这些没有"改前值")
--
--    所以启用本文件的同时, **要把 UPDATE / DELETE 那些 Service 方法上的
--    @OpLog 注解去掉**(OpType.UPDATE / OpType.DELETE / 批量删除)。
--    不去掉的话, 一次修改会产生两行日志, 一行有 before_value 一行没有。
--
--  ⚠️ 代价(必须接受): 触发器在业务事务内, 业务回滚则日志也回滚 ——
--     失败的操作(比如校验没过抛异常)不会留下任何记录。
--     而应用层切面用 REQUIRES_NEW 独立事务, 失败也记。
--     这是"能拿到改前值"换来的代价: 两者不可兼得。
--     若某张表的失败留痕比改前值更重要, 就别给它挂这个触发器, 继续用 @OpLog。
--
--  ⚠️ user_id 需要应用层配合: 触发器读 current_setting('app.user_id'),
--     应用层要在事务内调 set_config('app.user_id', <id>, true) 写进去。
--     没接的话 user_id 记为 null(见文件末尾"接线"说明)。
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

    if tg_op = 'INSERT' then
        v_type_id := 1;                                  -- INSERT
        v_before  := null;                               -- 新增没有"改前值"
        v_after   := to_jsonb(new)::text;
        if v_pk_col is not null then
            v_pk_val := to_jsonb(new) ->> v_pk_col;
        end if;

    elsif tg_op = 'UPDATE' then
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
--  ⚠️ 挂了触发器的表, 对应的 Service 写方法(新增除外)要把 @OpLog 去掉,
--     否则 UPDATE/DELETE 会被切面和触发器各记一遍。
--  ⚠️ 故意不挂 Log 表(会自己记自己, 无限递归) 和 Users 表(见文件头说明)。
-- ---------------------------------------------------------------------
create trigger trg_log_change_customer                 after insert or update or delete on Customer                 for each row execute function log_row_change();
create trigger trg_log_change_customer_status          after insert or update or delete on Customer_Status          for each row execute function log_row_change();
create trigger trg_log_change_orders                   after insert or update or delete on Orders                   for each row execute function log_row_change();
create trigger trg_log_change_order_status             after insert or update or delete on Order_Status             for each row execute function log_row_change();
create trigger trg_log_change_cargo_type               after insert or update or delete on Cargo_Type               for each row execute function log_row_change();
create trigger trg_log_change_cargo                    after insert or update or delete on Cargo                    for each row execute function log_row_change();
create trigger trg_log_change_cargo_container_result   after insert or update or delete on Cargo_Container_Result   for each row execute function log_row_change();
create trigger trg_log_change_container                after insert or update or delete on Container                for each row execute function log_row_change();
create trigger trg_log_change_container_trailer_record after insert or update or delete on Container_Trailer_Record for each row execute function log_row_change();
create trigger trg_log_change_container_type           after insert or update or delete on Container_Type           for each row execute function log_row_change();
create trigger trg_log_change_container_status         after insert or update or delete on Container_Status         for each row execute function log_row_change();
create trigger trg_log_change_company                  after insert or update or delete on Company                  for each row execute function log_row_change();
create trigger trg_log_change_trailer                  after insert or update or delete on Trailer                  for each row execute function log_row_change();
create trigger trg_log_change_container_event          after insert or update or delete on Container_Event          for each row execute function log_row_change();
create trigger trg_log_change_voyage                   after insert or update or delete on Voyage                   for each row execute function log_row_change();
create trigger trg_log_change_event_status             after insert or update or delete on Event_Status             for each row execute function log_row_change();
create trigger trg_log_change_vessel                   after insert or update or delete on Vessel                   for each row execute function log_row_change();
create trigger trg_log_change_ship_type                after insert or update or delete on Ship_Type                for each row execute function log_row_change();
create trigger trg_log_change_port                     after insert or update or delete on Port                     for each row execute function log_row_change();
create trigger trg_log_change_country                  after insert or update or delete on Country                  for each row execute function log_row_change();
create trigger trg_log_change_area                     after insert or update or delete on Area                     for each row execute function log_row_change();
create trigger trg_log_change_timezone                 after insert or update or delete on Timezone                 for each row execute function log_row_change();
create trigger trg_log_change_harbor_size              after insert or update or delete on Harbor_Size              for each row execute function log_row_change();
create trigger trg_log_change_port_level               after insert or update or delete on Port_Level               for each row execute function log_row_change();
create trigger trg_log_change_port_type                after insert or update or delete on Port_Type                for each row execute function log_row_change();
create trigger trg_log_change_operation_type           after insert or update or delete on Operation_Type           for each row execute function log_row_change();


-- ---------------------------------------------------------------------
--  接线: 让触发器能拿到 user_id
--
--  触发器跑在数据库里, 它不知道 HTTP 请求是谁发的, 所以要在同一个事务里
--  用 set_config 把当前用户写进"事务局部变量", 触发器再读出来:
--
--      -- 应用层在每个事务开始时执行一次(第三个参数 true = 事务结束自动失效)
--      select set_config('app.user_id', '7', true);
--
--  ⚠️ 第三个参数必须是 true。用 false 是会话级的, Tomcat 线程池复用连接时
--     会把上一个请求的用户留给下一个请求 —— 和 UserContext 那个坑一模一样。
--
--  ⚠️ 这条语句必须跑在**事务内部**, 否则 set_config(.., true) 立刻失效。
--     放到 AuthInterceptor 里不行(拦截器在事务外)。可行位置是 MyBatis 拦截器,
--     它在事务内、且和应用层写操作同一条连接:
--
--      @Intercepts(@Signature(type = Executor.class, method = "update",
--                             args = {MappedStatement.class, Object.class}))
--      public class AuditUserInterceptor implements Interceptor {
--          private final JdbcTemplate jdbc;
--          @Override
--          public Object intercept(Invocation inv) throws Throwable {
--              Long uid = UserContext.currentUserId();
--              if (uid != null) {
--                  jdbc.update("select set_config('app.user_id', ?, true)", uid.toString());
--              }
--              return inv.proceed();
--          }
--      }
--
--  ⚠️ 没接线时的表现: 日志照记, 只是 user_id 全是 null。不会报错。
--
--  ⚠️ 别忘了同步删掉 UPDATE/DELETE 方法上的 @OpLog, 否则会重复记两行。
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
--      for r in select event_object_table, trigger_name
--                 from information_schema.triggers
--                where trigger_name like 'trg_log_change%'
--      loop
--          execute format('drop trigger %I on %I', r.trigger_name, r.event_object_table);
--      end loop;
--  end $$;
--  -- drop function log_row_change();
-- ---------------------------------------------------------------------
