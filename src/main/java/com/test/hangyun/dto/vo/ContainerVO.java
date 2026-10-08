package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.Container;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 集装箱响应对象。
 * <p>
 * typeName / typeSize / ownerName / operatorName / statusDescription **都不是本表的列** ——
 * 库里没有 v_container 视图, 这几个字段由 Service 层分别查 container_type / company /
 * container_status 组装进来(见 ContainerServiceImpl)。
 * <p>
 * 两个标志位在这里是 **Boolean**, 库里存的是 char(1) '0'/'1', 转换在 Service 层做。
 */
@Data
public class ContainerVO {

    /** 箱号(主键) */
    private String no;

    private Long typeId;

    /** 箱型类别: 如普通箱 */
    private String typeName;

    /** 箱尺寸: 如40英尺 */
    private String typeSize;

    private Long ownerId;

    /** 箱主名称 */
    private String ownerName;

    private Long operatorId;

    /** 操作方名称 */
    private String operatorName;

    private String sealNo;

    private Long statusId;

    /** 状态中文描述 */
    private String statusDescription;

    /** 是否危险品(接口层用 true/false, 不是 '1'/'0') */
    private Boolean dangerFlag;

    /** 是否海事标识(接口层用 true/false) */
    private Boolean maritimeFlag;

    private String carrierOperate;

    private String ctrStatusTerminal;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;

    /**
     * 只填本表字段 + 转换两个标志位。
     * 跨表的名称字段(箱型/箱主/操作方/状态描述)由调用方查好后 set 进来。
     */
    public static ContainerVO from(Container e) {
        if (e == null) {
            return null;
        }
        ContainerVO vo = new ContainerVO();
        vo.setNo(e.getNo());
        vo.setTypeId(e.getTypeId());
        vo.setOwnerId(e.getOwnerId());
        vo.setOperatorId(e.getOperatorId());
        vo.setSealNo(e.getSealNo());
        vo.setStatusId(e.getStatusId());
        vo.setDangerFlag(toBoolean(e.getDangerFlag()));
        vo.setMaritimeFlag(toBoolean(e.getMaritimeFlag()));
        vo.setCarrierOperate(e.getCarrierOperate());
        vo.setCtrStatusTerminal(e.getCtrStatusTerminal());
        vo.setInsertTime(e.getInsertTime());
        vo.setUpdateTime(e.getUpdateTime());
        return vo;
    }

    /** '1' -> true, '0' 或 null -> false */
    private static Boolean toBoolean(String flag) {
        return "1".equals(flag);
    }
}
