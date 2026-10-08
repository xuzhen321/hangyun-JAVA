package com.test.hangyun.excel;

import java.util.function.Function;

/**
 * 导出的一列: 表头文字 + 怎么从一行数据里取值。
 * <p>
 * ⚠️ **表头必须写中文描述, 不是字段名**; 字典编码也要在取值函数里翻成中文
 * (比如 {@code resultStatus} 存的 '1'/'0', 导出要写"成功"/"失败") —— 导出的 Excel
 * 是给人看的, 出现 {@code '0'} 或者 {@code statusId} 都算没做完。
 *
 * @param header 表头文字
 * @param value  从一行记录取值。返回 null 时该单元格留空
 * @param <T>    行数据的类型(一般是某个 VO)
 */
public record ExcelColumn<T>(String header, Function<T, Object> value) {

    public static <T> ExcelColumn<T> of(String header, Function<T, Object> value) {
        return new ExcelColumn<>(header, value);
    }
}
