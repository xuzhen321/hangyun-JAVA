package com.test.hangyun.constant;

/**
 * Excel 导出的约定。
 */
public final class ExportConstants {

    /**
     * 单次导出允许的最大行数, 超过直接 400。
     * <p>
     * 目的不是"省内存"(导出的数据本来就要全读出来), 而是**兜住失控的查询**:
     * 一个不带筛选条件的导出等于把整张表拖出来, 既拖慢自己也可能把库压住。
     * 提示用户加筛选条件比默默跑十分钟要好。
     */
    public static final int MAX_ROWS = 50_000;

    /**
     * SXSSF 在内存里保留的行数(滑动窗口)。
     * <p>
     * 超出的行会被刷到磁盘临时文件, 所以内存占用和总行数无关, 只和这个值有关。
     * 100 行对导出这种"顺序写、不回头读"的场景绰绰有余。
     */
    public static final int STREAM_WINDOW_ROWS = 100;

    /**
     * 响应头里的 MIME 类型。.xlsx 的标准写法, 别写成 {@code application/vnd.ms-excel}
     * (那是老的 .xls)。
     */
    public static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /**
     * 导出文件名统一用**英文资源名 + 日期**, 如 {@code logs_20261008.xlsx}。
     * <p>
     * 有意避开中文文件名: 带中文的 {@code Content-Disposition} 要做 RFC 5987 的
     * {@code filename*=UTF-8''...} 编码, 老浏览器还认不了, 属于纯粹的坑。
     * 英文名一个都不用处理, 前端需要的话自己在下载时改名即可。
     */
    public static final String FILE_NAME_PATTERN = "%s_%s.xlsx";

    /** 工具类, 不允许实例化 */
    private ExportConstants() {
    }
}
