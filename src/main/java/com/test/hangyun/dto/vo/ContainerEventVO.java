package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.enums.EstimateFlag;
import com.test.hangyun.pojo.enums.EventSource;
import com.test.hangyun.pojo.view.ContainerEventView;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物流事件响应对象。
 * <p>
 * 所有字段都来自**视图 v_container_event** —— 船名、航次号、发生地港口、状态中英文、时区
 * 视图里已经联好了, 后端不用再做二次组装。
 * <p>
 * 两个标志位在 JSON 里是**中文文字**("船公司"/"港区"、"实际"/"预计"), 不是编码。
 */
@Data
public class ContainerEventVO {

    private Long id;

    /** 箱号 */
    private String containerNo;

    /** 船名(来自 vessel.name; 船舶没关联时为空) */
    private String vslName;

    /** 航次号(来自 voyage.no) */
    private String voy;

    /** 事件状态ID */
    private Long eventStatusId;

    /** 事件状态中文描述 */
    private String descriptionCn;

    /** 事件状态英文描述 */
    private String descriptionEn;

    /** 状态发生时间 */
    private LocalDateTime eventTime;

    /** "实际" / "预计" */
    private EstimateFlag isEsti;

    /** 发生地ID */
    private Long eventPlaceId;

    /** 发生地港口中文名 */
    private String eventPlace;

    /** 发生地港口五字码 */
    private String portCode;

    /** 港口时区 */
    private String portTimeZone;

    /** 港口时区(+8) */
    private String portTimeZone2;

    /** "船公司" / "港区" */
    private EventSource source;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;

    public static ContainerEventVO from(ContainerEventView v) {
        if (v == null) {
            return null;
        }
        ContainerEventVO vo = new ContainerEventVO();
        vo.setId(v.getId());
        vo.setContainerNo(v.getContainerNo());
        vo.setVslName(v.getVslName());
        vo.setVoy(v.getVoy());
        vo.setEventStatusId(v.getEventStatusId());
        vo.setDescriptionCn(v.getDescriptionCn());
        vo.setDescriptionEn(v.getDescriptionEn());
        vo.setEventTime(v.getEventTime());
        vo.setIsEsti(v.getIsEsti());
        vo.setEventPlaceId(v.getEventPlaceId());
        vo.setEventPlace(v.getEventPlace());
        vo.setPortCode(v.getPortCode());
        vo.setPortTimeZone(v.getPortTimeZone());
        vo.setPortTimeZone2(v.getPortTimeZone2());
        vo.setSource(v.getSource());
        vo.setInsertTime(v.getInsertTime());
        vo.setUpdateTime(v.getUpdateTime());
        return vo;
    }
}
