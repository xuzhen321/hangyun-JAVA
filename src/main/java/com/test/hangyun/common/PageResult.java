package com.test.hangyun.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.function.Function;

/**
 * 统一分页结果, 作为 Result.data 返回。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /** 总条数 */
    private long total;
    /** 当前页码, 从 1 开始 */
    private long page;
    /** 每页条数 */
    private long size;
    /** 当前页数据 */
    private List<T> list;

    /** 直接由 MyBatis-Plus 的 IPage 构建 */
    public static <T> PageResult<T> of(IPage<T> p) {
        return new PageResult<>(p.getTotal(), p.getCurrent(), p.getSize(), p.getRecords());
    }

    /** 由 IPage 构建, 并把每条记录转换成另一种类型 */
    public static <E, T> PageResult<T> of(IPage<E> p, Function<E, T> mapper) {
        return new PageResult<>(p.getTotal(), p.getCurrent(), p.getSize(),
                p.getRecords().stream().map(mapper).toList());
    }
}
