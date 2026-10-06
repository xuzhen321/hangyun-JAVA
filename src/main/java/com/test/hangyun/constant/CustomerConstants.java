package com.test.hangyun.constant;

/**
 * 客户模块的通用约定。
 */
public final class CustomerConstants {

    /**
     * 批量删除一次最多允许的 id 数量。
     * <p>
     * 不是业务限制, 是防御性的: 不设上限的话, 一个请求就能拼出几万个 IN 参数,
     * 既拖慢数据库也容易被滥用。前端多选删除时按这个上限分批提交即可。
     */
    public static final int MAX_BATCH_DELETE_SIZE = 100;

    /** 工具类, 不允许实例化 */
    private CustomerConstants() {
    }
}
