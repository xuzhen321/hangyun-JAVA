package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.ContainerEventBatchDeleteReq;
import com.test.hangyun.dto.ContainerEventQueryReq;
import com.test.hangyun.dto.ContainerEventReq;
import com.test.hangyun.dto.vo.ContainerEventVO;
import com.test.hangyun.excel.ExcelColumn;
import com.test.hangyun.excel.ExcelExporter;
import com.test.hangyun.excel.ExcelResponse;
import com.test.hangyun.service.ContainerEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 集装箱物流事件。
 * <p>
 * 集装箱的**轨迹接口**（{@code GET /containers/{no}/track}）和概览在
 * {@link ContainerController} 里 —— 那两条路径挂在 /containers 下面。
 */
@Tag(name = "物流事件", description = "集装箱物流事件的增删改查")
@RestController
@RequestMapping("/container-events")
@RequiredArgsConstructor
public class ContainerEventController {

    private final ContainerEventService containerEventService;

    private final ExcelExporter excelExporter;

    /**
     * 导出列。**表头用中文**, 两个标志位导中文文字而不是编码:
     * {@code isEsti} 导"实际"/"预计", {@code source} 导"船公司"/"港区"。
     */
    private static final List<ExcelColumn<ContainerEventVO>> EXPORT_COLUMNS = List.of(
            ExcelColumn.of("事件ID", ContainerEventVO::getId),
            ExcelColumn.of("箱号", ContainerEventVO::getContainerNo),
            ExcelColumn.of("船名", ContainerEventVO::getVslName),
            ExcelColumn.of("航次号", ContainerEventVO::getVoy),
            ExcelColumn.of("事件状态", ContainerEventVO::getDescriptionCn),
            ExcelColumn.of("发生时间", ContainerEventVO::getEventTime),
            ExcelColumn.of("实际/预计", vo -> vo.getIsEsti() == null ? null : vo.getIsEsti().getText()),
            ExcelColumn.of("发生地", ContainerEventVO::getEventPlace),
            ExcelColumn.of("港口五字码", ContainerEventVO::getPortCode),
            ExcelColumn.of("数据来源", vo -> vo.getSource() == null ? null : vo.getSource().getText()),
            ExcelColumn.of("录入时间", ContainerEventVO::getInsertTime),
            ExcelColumn.of("更新时间", ContainerEventVO::getUpdateTime)
    );

    @Operation(summary = "分页查询物流事件(可按箱号精确、状态精确、发生时间区间筛选)")
    @GetMapping
    public Result<PageResult<ContainerEventVO>> page(ContainerEventQueryReq req) {
        return Result.success(containerEventService.page(req));
    }

    // ⚠️ /export 必须写在 /{id} **前面**。
    @Operation(summary = "导出物流事件为 Excel(筛选条件与列表一致, 不分页)")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(ContainerEventQueryReq req) {
        List<ContainerEventVO> rows = containerEventService.listForExport(req);
        byte[] body = excelExporter.toXlsx("物流事件", EXPORT_COLUMNS, rows);
        return ExcelResponse.of("container-events", body);
    }

    @Operation(summary = "查询物流事件详情")
    @GetMapping("/{id}")
    public Result<ContainerEventVO> getById(@PathVariable Long id) {
        return Result.success(containerEventService.getById(id));
    }

    @Operation(summary = "新增物流事件")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ContainerEventReq req) {
        containerEventService.create(req);
        return Result.success();
    }

    @Operation(summary = "修改物流事件")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ContainerEventReq req) {
        containerEventService.update(id, req);
        return Result.success();
    }

    @Operation(summary = "批量删除物流事件(逻辑删除, 状态改为已删除)")
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@Valid @RequestBody ContainerEventBatchDeleteReq req) {
        containerEventService.deleteBatch(req.getIds());
        return Result.success();
    }

    @Operation(summary = "删除物流事件(逻辑删除, 状态改为已删除)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        containerEventService.delete(id);
        return Result.success();
    }
}
