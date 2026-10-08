package com.test.hangyun.dto.vo;

import com.test.hangyun.pojo.entity.Trailer;
import lombok.Data;

/**
 * 拖车下拉框候选项（给"新增提空箱记录"选提空箱拖车用）。
 * <p>
 * 拖车号本身就是用户看得懂的标识（车牌号）, 带上司机姓名是为了让用户确认找对了车——
 * 同一个车队可能有多辆车, 光看车牌不一定记得是谁在开。
 */
@Data
public class TrailerOptionVO {

    /** 拖车号，**提交提空箱记录时填这个**（trackNo） */
    private String no;

    /** 司机姓名 */
    private String name;

    public static TrailerOptionVO from(Trailer e) {
        if (e == null) {
            return null;
        }
        TrailerOptionVO vo = new TrailerOptionVO();
        vo.setNo(e.getNo());
        vo.setName(e.getName());
        return vo;
    }
}
