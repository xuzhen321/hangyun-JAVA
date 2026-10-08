package com.test.hangyun.log;

import com.test.hangyun.pojo.enums.OpType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记"这个方法是要记操作日志的", 由 {@link LogAspect} 切面负责落库。
 * <p>
 * 用法 —— 标在 <b>Service 方法</b>上(不要标在 Controller 上, 那样会连参数校验失败的
 * 请求也记一笔):
 *
 * <pre>{@code
 * @OpLog(module = "集装箱信息管理", table = "container", type = OpType.UPDATE, desc = "更新集装箱")
 * public void update(String no, ContainerUpdateReq req) { ... }
 * }</pre>
 * <p>
 * ⚠️ 标注的方法**自身不要吞异常** —— 切面靠异常来判断这次操作是成功还是失败
 * ({@code result_status} 的 1/0)。
 * <p>
 * 📌 **唯一的例外是 {@link OpType#EXPORT}**: 它标在各资源的 {@code listForExport} 上,
 * 那是个读方法。因为"导出"本身是个**只读动作**(不落库), 但业务上必须留痕 ——
 * 设计文档 5.4 明确要求"导出也要记入操作日志"。
 * <p>
 * ⚠️ 由此带来一个已知的小失真: 日志记的是"取到了要导出的数据", 而不是"文件成功下载"。
 * 如果 {@code listForExport} 之后生成 Excel 那一步失败, 日志里仍然是成功。
 * 要精确到那一步, 得把注解挪到 Controller 的 export 方法上 —— 但那会破坏
 * "日志切面只切 Service" 这条统一约定, 收益不值这个代价。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OpLog {

    /** 所属模块, 如"集装箱信息管理"。写进日志便于按模块筛选 */
    String module();

    /** 操作类型。用枚举而不是字符串, 写错了编译不过 */
    OpType type();

    /** 这次操作在做什么, 如"更新集装箱状态"。写具体一点, 审计时才有用 */
    String desc();

    /**
     * 目标表名, 如 {@code container}。可空 —— 有些操作没有单一目标表。
     * <p>
     * 填了的话日志里能直接看出"动了哪张表"。
     */
    String table() default "";

    /**
     * 目标记录主键。可空, 空则按约定取**第一个方法参数**。
     * <p>
     * 需要更复杂的取法时用 SpEL, 引用参数名即可(本项目编译时开了 {@code -parameters}):
     * {@code targetId = "#no"} 取名为 no 的参数。
     * <p>
     * 注意: {@code create} 这种整体覆盖的新增拿不到 id(插入前还没有), 留空即可。
     */
    String targetId() default "";
}
