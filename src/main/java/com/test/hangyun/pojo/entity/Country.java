package com.test.hangyun.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 国家字典表 country 。
 * <p>
 * 被两处引用: vessel.country_id(船旗国)、port.country_id(港口所在国家)。
 * country_code 在库里有唯一约束, 所以新增/修改时要查重。
 */
@Data
@TableName("country")
public class Country {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 国家代码, 如 CN。库里有唯一约束 */
    private String countryCode;

    /** 国家英文名 */
    private String countryEnname;

    /** 国家中文名 */
    private String countryCnname;

    /** 由数据库触发器维护 */
    private LocalDateTime insertTime;

    /** 由数据库触发器维护 */
    private LocalDateTime updateTime;
}
