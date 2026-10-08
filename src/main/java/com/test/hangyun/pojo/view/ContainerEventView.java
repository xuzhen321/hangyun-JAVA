package com.test.hangyun.pojo.view;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.test.hangyun.pojo.enums.EstimateFlag;
import com.test.hangyun.pojo.enums.EventSource;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 集装箱物流事件视图 v_container_event （只读）。
 * <p>
 * 这个视图把**船名、航次号、发生地港口、事件状态、时区都联好了** ——
 * 所以这个模块不用像货物/集装箱那样在 Service 层分步组装, 一次查询就拿到全部 ✅
 * <p>
 * 对应列: id, container_no, vsl_name, voy, event_status_id, description_cn, description_en,
 *         event_time, is_esti, event_place_id, event_place, port_code,
 *         port_time_zone, port_time_zone2, source, insert_time, update_time
 */
@Data
@TableName("v_container_event")
public class ContainerEventView {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /** 箱号 */
    private String containerNo;

    /** 来自 vessel.name */
    private String vslName;

    /** 来自 voyage.no(航次号) */
    private String voy;

    /** 事件状态ID */
    private Long eventStatusId;

    /** 来自 event_status.description_cn */
    private String descriptionCn;

    /** 来自 event_status.description_en */
    private String descriptionEn;

    /** 状态发生时间 */
    private LocalDateTime eventTime;

    /** 实际发生 / 预计发生 */
    private EstimateFlag isEsti;

    /** 发生地ID, 逻辑外键 -> port.id */
    private Long eventPlaceId;

    /** 来自 port.cnname(发生地港口中文名) */
    private String eventPlace;

    /** 来自 port.unlocode(港口五字码) */
    private String portCode;

    /** 来自 timezone.timezone_UTC8 */
    private String portTimeZone;

    /** 来自 timezone.timezone_Asia_Shanghai */
    private String portTimeZone2;

    /** 数据来源: 船公司 / 港区 */
    private EventSource source;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;
}
