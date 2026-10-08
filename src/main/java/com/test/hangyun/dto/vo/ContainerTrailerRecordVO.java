package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.ContainerTrailerRecord;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提空箱记录响应对象。
 * <p>
 * driverName 不是本表的列 —— 库里没有对应视图, 它由 Service 层沿
 * container_trailer_record -> trailer 查出来后填进来。
 */
@Data
public class ContainerTrailerRecordVO {

    /** 记录ID。自增数字, 没有精度问题, 可以直接当数字用 */
    private Long id;

    /** 箱号 */
    private String containerNo;

    /** 提空箱拖车号 */
    private String trackNo;

    /** 司机姓名(来自 trailer.name; 由 Service 组装) */
    private String driverName;

    /** 提空箱拖车进场时间 */
    private LocalDateTime dcInDate;

    /** 提空箱拖车出场时间 */
    private LocalDateTime dcOutDate;

    /** 箱使用备注 */
    private String useNote;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;

    /** driverName 由调用方查好传进来, 因为它在 trailer 表上 */
    public static ContainerTrailerRecordVO from(ContainerTrailerRecord e, String driverName) {
        if (e == null) {
            return null;
        }
        ContainerTrailerRecordVO vo = new ContainerTrailerRecordVO();
        vo.setId(e.getId());
        vo.setContainerNo(e.getContainerNo());
        vo.setTrackNo(e.getTrackNo());
        vo.setDriverName(driverName);
        vo.setDcInDate(e.getDcInDate());
        vo.setDcOutDate(e.getDcOutDate());
        vo.setUseNote(e.getUseNote());
        vo.setInsertTime(e.getInsertTime());
        vo.setUpdateTime(e.getUpdateTime());
        return vo;
    }
}
