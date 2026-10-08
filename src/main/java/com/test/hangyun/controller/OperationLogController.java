package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.OperationLogQueryReq;
import com.test.hangyun.dto.vo.OperationLogVO;
import com.test.hangyun.excel.ExcelColumn;
import com.test.hangyun.excel.ExcelExporter;
import com.test.hangyun.excel.ExcelResponse;
import com.test.hangyun.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 操作日志。**只读** —— 没有增删改接口。
 * <p>
 * ⚠️ 这是全项目**第一个带 {@code /export} 的资源**, 也是导出接口的样板:
 * 返回 {@code ResponseEntity<byte[]>} 而不是 {@code Result}(唯一的例外)。
 */
@Tag(name = "操作日志", description = "操作日志查询与导出（只读，无增删改）")
@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class OperationLogController {

    /**
     * 导出列。**表头与列表页一致**, 且字典编码要翻成中文:
     * {@code resultStatus} 库里存的是 '1'/'0', 导出写"成功"/"失败" ——
     * 导出的 Excel 是给人看的, 出现 '0' 或者字段名都算没做完。
     */
    private static final List<ExcelColumn<OperationLogVO>> EXPORT_COLUMNS = List.of(
            ExcelColumn.of("日志ID", OperationLogVO::getId),
            ExcelColumn.of("操作人", OperationLogVO::getOperatorName),
            ExcelColumn.of("操作类型", OperationLogVO::getOperationType),
            ExcelColumn.of("目标表", OperationLogVO::getTargetTable),
            ExcelColumn.of("目标记录", OperationLogVO::getTargetId),
            ExcelColumn.of("操作时间", OperationLogVO::getOperationTime),
            ExcelColumn.of("执行结果", vo -> resultText(vo.getResultStatus())),
            ExcelColumn.of("错误信息", OperationLogVO::getErrorMessage),
            ExcelColumn.of("操作前数据", OperationLogVO::getBeforeValue),
            ExcelColumn.of("操作后数据", OperationLogVO::getAfterValue)
    );

    private final OperationLogService operationLogService;
    private final ExcelExporter excelExporter;

    @Operation(summary = "分页查询操作日志(按操作人/类型/目标表/结果/时间范围筛选)")
    @GetMapping
    public Result<PageResult<OperationLogVO>> page(OperationLogQueryReq req) {
        return Result.success(operationLogService.page(req));
    }

    // ⚠️ /export 必须写在 /{id} **前面**。虽然 Spring 会优先匹配字面量路径,
    //    但把具体路径放在前面更不容易误读。
    @Operation(summary = "导出操作日志为 Excel(筛选条件与列表一致, 不分页)")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(OperationLogQueryReq req) {
        List<OperationLogVO> rows = operationLogService.listForExport(req);
        byte[] body = excelExporter.toXlsx("操作日志", EXPORT_COLUMNS, rows);
        return ExcelResponse.of("logs", body);
    }

    @Operation(summary = "查询操作日志详情")
    @GetMapping("/{id}")
    public Result<OperationLogVO> getById(@PathVariable Long id) {
        return Result.success(operationLogService.getById(id));
    }

    /** 结果编码转中文, 库里存 '1'/'0' */
    private static String resultText(String resultStatus) {
        if ("1".equals(resultStatus)) {
            return "成功";
        }
        if ("0".equals(resultStatus)) {
            return "失败";
        }
        return "";
    }
}
