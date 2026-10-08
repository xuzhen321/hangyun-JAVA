package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.ContainerType;
import lombok.Data;

/**
 * 集装箱箱型下拉框候选项（给"新增集装箱"选箱型用）。
 * <p>
 * 只带 id + 类别 + 尺寸。类别和尺寸**分成两个字段返回** —— 前端拼起来显示即可
 * （如"普通箱 / 40英尺"），不要指望后端拼好(后端不知道该用什么分隔符)。
 */
@Data
public class ContainerTypeOptionVO {

    /** 提交集装箱时要用这个 id（typeId） */
    private Long id;

    /** 箱型类别 */
    private String type;

    /** 箱尺寸 */
    private String size;

    public static ContainerTypeOptionVO from(ContainerType e) {
        if (e == null) {
            return null;
        }
        ContainerTypeOptionVO vo = new ContainerTypeOptionVO();
        vo.setId(e.getId());
        vo.setType(e.getType());
        vo.setSize(e.getSize());
        return vo;
    }
}
