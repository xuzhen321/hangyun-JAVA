package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.view.CustomerView;

/**
 * 客户管理视图 v_customer 的只读查询。
 * 视图是多表连接, 不可更新, 这里只调用 select 方法。
 */
public interface CustomerViewMapper extends BaseMapper<CustomerView> {
}
