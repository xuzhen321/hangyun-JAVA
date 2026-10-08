package com.test.hangyun.service;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.dto.TrailerCreateReq;
import com.test.hangyun.dto.TrailerQueryReq;
import com.test.hangyun.dto.TrailerUpdateReq;
import com.test.hangyun.dto.vo.TrailerOptionVO;
import com.test.hangyun.dto.vo.TrailerVO;

import java.util.List;

public interface TrailerService {

    /** 分页查询, 支持按拖车号/司机姓名前缀搜索 */
    PageResult<TrailerVO> page(TrailerQueryReq req);

    /** 详情 */
    TrailerVO getById(String no);

    /** 新增: 拖车号由前端提供, 必填且不可重复 */
    void create(TrailerCreateReq req);

    /** 修改: **改不了拖车号**(主键) */
    void update(String no, TrailerUpdateReq req);

    /** 删除, 有提空箱记录用了这个拖车时拒绝 */
    void delete(String no);

    /** 下拉框候选(给新增提空箱记录选拖车用): 最多 20 条, 返回拖车号+司机姓名 */
    List<TrailerOptionVO> options(String keyword);
}
