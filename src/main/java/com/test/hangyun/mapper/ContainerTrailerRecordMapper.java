package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.ContainerTrailerRecord;

/**
 * 集装箱拖车记录表 container_trailer_record （提空箱登记）。
 * <p>
 * 库里没有对应的视图, 所以直接读基础表。
 * 拖车司机姓名由 Service 层再查一次 trailer 组装进来(见 ContainerTrailerRecordServiceImpl)。
 * <p>
 * 这里没有"引用检查"方法: 这张表是链路末端, 没有别的表引用它, 物理删除即可。
 */
public interface ContainerTrailerRecordMapper extends BaseMapper<ContainerTrailerRecord> {
}
