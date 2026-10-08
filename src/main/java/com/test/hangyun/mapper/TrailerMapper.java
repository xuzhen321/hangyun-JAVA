package com.test.hangyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.test.hangyun.pojo.entity.Trailer;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 拖车信息表 trailer 的写操作 + 引用检查。
 */
public interface TrailerMapper extends BaseMapper<Trailer> {

    /**
     * 统计有多少条提空箱记录用了这个拖车。
     * <p>
     * container_trailer_record.track_no 逻辑引用 trailer.no, 库里没有物理外键,
     * 所以物理删除前必须由应用层自己检查引用, 否则会留下悬空的拖车号。
     */
    @Select("select count(*) from container_trailer_record where track_no = #{no}")
    long countRecordsByTrailerNo(@Param("no") String no);
}
