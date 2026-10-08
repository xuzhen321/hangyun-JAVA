package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.OperationType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作类型响应对象。
 * <p>
 * ⚠️ 没有 {@code builtin} 这类"是否内置"字段 —— 这张表**五行全都是内置的**
 * (不支持新增删除, 见 Controller 的类注释), 每行都标成 true 等于没信息。
 */
@Data
public class OperationTypeVO {

    private Long id;

    /** 操作类型: INSERT / UPDATE / DELETE / EXPORT / LOGIN */
    private String type;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;

    public static OperationTypeVO from(OperationType e) {
        if (e == null) {
            return null;
        }
        OperationTypeVO vo = new OperationTypeVO();
        vo.setId(e.getId());
        vo.setType(e.getType());
        vo.setInsertTime(e.getInsertTime());
        vo.setUpdateTime(e.getUpdateTime());
        return vo;
    }
}
