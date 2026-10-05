-- =====================================================================
--  航运项目 视图初始化脚本 (PostgreSQL)
--  依据《第四周报告》中的功能模块编写, 共 7 个视图。
--  第四周报告中的每张"表"都是扁平形状, 正好对应第六周规范化表的一次联表查询。
--
--  视图清单:
--    v_customer         客户管理          Customer + Customer_Status
--    v_order            订单管理          Orders + Customer + Port x2 + Order_Status
--    v_container_event  集装箱物流信息    Container_Event + Voyage + Vessel + Port
--                                        + Event_Status + Timezone
--    v_vessel           船舶信息管理      Vessel + Ship_Type + Country + Company x2
--    v_port             港口信息管理      Port + Country + Area + Timezone + Harbor_Size
--                                        + Port_Level + Port_Type + Port 自连接
--    v_user             系统用户          Users
--    v_operation_log    操作日志          Log + Users + Operation_Type
--
--  编写约定:
--    1. 视图仅供查询使用, 不做增删改。均为多表连接, PostgreSQL 下本身不可更新。
--    2. 报告中标注的"逻辑外键"没有物理约束, 对应记录可能不存在,
--       因此全部使用 left join, 避免因悬空外键丢行。
--    3. 报告中存在但第六周表结构中没有的字段(如 event_code、event_place_origin、
--       module、operation_desc、delete_flag)未包含在本脚本中。
--    4. GT / NT / DWT / timezone_UTC8 / timezone_Asia_Shanghai 是带引号的列名,
--       引用时必须保留双引号。
-- =====================================================================

begin;

-- =====================================================================
-- 1. 客户管理
-- =====================================================================
create or replace view v_customer as
select
    c.id                      as id,
    c.name                    as name,
    c.phone                   as phone,
    c.email                   as email,
    c.address                 as address,
    c.qualification           as qualification,
    c.qualification_valid_to  as qualification_valid_to,
    c.status_id               as status,
    cs.description            as status_description,
    c.insert_time             as insert_time,
    c.update_time             as update_time
from Customer c
left join Customer_Status cs on cs.id = c.status_id;

comment on view v_customer is '客户管理视图: 客户基本信息 + 客户状态描述';

-- =====================================================================
-- 2. 订单管理
-- =====================================================================
create or replace view v_order as
select
    o.id                 as id,
    o.customer_id        as customer_id,
    c.name               as customer_name,
    o.order_date         as order_date,
    o.loading_port_id    as loading_port_id,
    lp.cnname            as loading_port_name,
    o.discharge_port_id  as discharge_port_id,
    dp.cnname            as discharge_port_name,
    o.status_id          as status,
    os.description       as status_description,
    o.insert_time        as insert_time,
    o.update_time        as update_time
from Orders o
left join Customer c      on c.id  = o.customer_id
left join Port lp         on lp.id = o.loading_port_id
left join Port dp         on dp.id = o.discharge_port_id
left join Order_Status os on os.id = o.status_id;

comment on view v_order is '订单管理视图: 订单 + 客户名称 + 起运港/目的港名称 + 订单状态描述';

-- =====================================================================
-- 3. 集装箱物流信息管理
-- 注意: 发生地(event_place_id)关联港口表, 港口再关联时区表。
-- =====================================================================
create or replace view v_container_event as
select
    ce.id                        as id,
    ce.container_no              as container_no,
    v.name                       as vsl_name,
    vg.no                        as voy,
    ce.event_status_id           as event_status_id,
    est.description_cn           as description_cn,
    est.description_en           as description_en,
    ce.event_time                as event_time,
    ce.is_esti                   as is_esti,
    ce.event_place_id            as event_place_id,
    p.cnname                     as event_place,
    p.unlocode                   as port_code,
    tz."timezone_UTC8"           as port_time_zone,
    tz."timezone_Asia_Shanghai"  as port_time_zone2,
    ce.source                    as source,
    ce.insert_time               as insert_time,
    ce.update_time               as update_time
from Container_Event ce
left join Voyage vg       on vg.id  = ce.voyage_id
left join Vessel v        on v.id   = vg.vsl_id
left join Port p          on p.id   = ce.event_place_id
left join Event_Status est on est.id = ce.event_status_id
left join Timezone tz     on tz.id  = p.timezone_id;

comment on view v_container_event is '集装箱物流信息视图: 事件 + 船名/航次 + 发生地港口 + 事件状态 + 时区';

-- =====================================================================
-- 4. 船舶信息管理
-- =====================================================================
create or replace view v_vessel as
select
    v.id           as id,
    v.mmsi         as mmsi,
    v.name         as shipname,
    v.imo          as imo,
    v.callsign     as callsign,
    co.country_cnname  as flag_state,
    st.type        as shiptype,
    st."GT"        as "GT",
    st."NT"        as "NT",
    st."DWT"       as "DWT",
    st.length      as length,
    st.width       as width,
    v.build_year   as build_year,
    own.name       as ship_owner,
    own.code       as ship_owner_code,
    mgr.name       as ship_manager,
    mgr.code       as ship_manager_code,
    v.insert_time  as insert_time,
    v.update_time  as update_time
from Vessel v
left join Ship_Type st on st.id  = v.vessel_type_id
left join Country co   on co.id  = v.country_id
left join Company own  on own.id = v.owner_company_id
left join Company mgr  on mgr.id = v.manager_company_id;

comment on view v_vessel is '船舶信息视图: 船舶 + 船旗国 + 船舶类型吨位尺度 + 船东/管理公司';

-- =====================================================================
-- 5. 港口信息管理
-- =====================================================================
create or replace view v_port as
select
    p.id                         as port_id,
    p.unlocode                   as port_unlocode,
    p.enname                     as port_enname,
    p.cnname                     as port_cnname,
    p.latitude                   as latitude,
    p.longitude                  as longitude,
    p.geom                       as geom,
    p.province                   as province,
    co.country_code              as country_code,
    co.country_enname            as country_enname,
    co.country_cnname            as country_cnname,
    a.area_name                  as area_name,
    hs.size                      as harbor_size,
    pl.level                     as level,
    pt.type                      as port_type,
    p.parent_port_id             as parent_port_id,
    pp.cnname                    as parent_port_name,
    tz."timezone_UTC8"           as timezone,
    tz."timezone_Asia_Shanghai"  as timezone2,
    p.state                      as state,
    p.insert_time                as insert_time,
    p.update_time                as update_time
from Port p
left join Country co     on co.id = p.country_id
left join Area a         on a.id  = p.area_id
left join Timezone tz    on tz.id = p.timezone_id
left join Harbor_Size hs on hs.id = p.harbor_size_id
left join Port_Level pl  on pl.id = p.level_id
left join Port_Type pt   on pt.id = p.port_type_id
left join Port pp        on pp.id = p.parent_port_id;

comment on view v_port is '港口信息视图: 港口 + 国家 + 区域 + 时区 + 尺寸/级别/类型 + 母港';

-- =====================================================================
-- 6. 系统用户
-- =====================================================================
create or replace view v_user as
select
    u.id          as user_id,
    u.username    as username,
    u.password    as password_hash,
    u.real_name   as real_name,
    u.phone       as phone,
    u.status      as status,
    u.insert_time as insert_time,
    u.update_time as update_time
from Users u;

comment on view v_user is '系统用户视图: 用户账号信息';

-- =====================================================================
-- 7. 操作日志
-- =====================================================================
create or replace view v_operation_log as
select
    l.id             as log_id,
    l.user_id        as user_id,
    u.real_name      as operator_name,
    l.type_id        as operation_type_id,
    ot.type          as operation_type,
    l.target_table   as target_table,
    l.target_id      as target_id,
    l.operation_time as operation_time,
    l.before_value   as before_value,
    l.after_value    as after_value,
    l.result_status  as result_status,
    l.error_message  as error_message
from Log l
left join Users u           on u.id  = l.user_id
left join Operation_Type ot on ot.id = l.type_id;

comment on view v_operation_log is '操作日志视图: 日志 + 操作人姓名 + 操作类型';

commit;