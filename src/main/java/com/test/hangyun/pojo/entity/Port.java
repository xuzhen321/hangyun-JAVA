package com.test.hangyun.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 港口表 port 。
 * <p>
 * 目前只服务于两件事:
 * <ul>
 *   <li>订单的起运港/目的港**引用校验**(selectById 判断存在性)</li>
 *   <li>港口**下拉框搜索**(cnname / enname / unlocode 三个字段)</li>
 * </ul>
 * 所以只映射了这几列。/ports 做成完整资源(列表、详情、增删改)时, 再按 initial.sql 补全其余字段。
 * <p>
 * ⚠️ 字段不全, 不要拿它去接列表/详情查询 —— 没映射的列会静默为 null。
 * 港口列表/详情将来要走视图 v_port(那里已经联好了国家、区域、时区等)。
 */
@Data
@TableName("port")
public class Port {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 港口五字码(UN/LOCODE), 如 CNSHA。库里有唯一约束 */
    private String unlocode;

    /** 港口英文名 */
    private String enname;

    /** 港口中文名 */
    private String cnname;

    /**
     * 数据状态: 0默认, 1新增, 2修改, 3删除。
     * 港口是**逻辑删除**, 查询时必须排除 3(见 PortConstants.STATE_DELETED)。
     * 该列允许为 null, 用 "state <> '3'" 过滤会连 null 一起漏掉, 要额外放行。
     */
    private String state;
}
