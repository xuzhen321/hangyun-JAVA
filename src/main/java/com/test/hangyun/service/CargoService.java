package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.CargoCreateReq;
import com.test.hangyun.dto.CargoQueryReq;
import com.test.hangyun.dto.CargoUpdateReq;
import com.test.hangyun.dto.vo.CargoVO;

import java.util.List;

public interface CargoService {

    /** 分页查询, 支持按货物名称(前缀)和订单号(精确)筛选 */
    PageResult<CargoVO> page(CargoQueryReq req);

    /** 新增: 必须指定订单号(货物不能凭空建) */
    void create(CargoCreateReq req);

    /** 修改: 只能改货物种类和数量, **改不了所属订单** */
    void update(Long id, CargoUpdateReq req);

    /** 删除, 有装箱结果引用时拒绝 */
    void delete(Long id);

    /** 批量删除。严格语义: 只要有一条被引用就整批拒绝; 不存在的 id 忽略 */
    void deleteBatch(List<Long> ids);
}
