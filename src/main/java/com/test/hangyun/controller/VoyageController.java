package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.VoyageQueryReq;
import com.test.hangyun.dto.VoyageReq;
import com.test.hangyun.dto.vo.VoyageVO;
import com.test.hangyun.excel.ExcelColumn;
import com.test.hangyun.excel.ExcelExporter;
import com.test.hangyun.excel.ExcelResponse;
import com.test.hangyun.service.VoyageService;
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

@Tag(name = "航次管理", description = "航次信息的增删改查")
@RestController
@RequestMapping("/voyages")
@RequiredArgsConstructor
public class VoyageController {

    private final VoyageService voyageService;

    private final ExcelExporter excelExporter;

    /** 导出列。**表头用中文**, 与列表列头一致 */
    private static final List<ExcelColumn<VoyageVO>> EXPORT_COLUMNS = List.of(
            ExcelColumn.of("航次ID", VoyageVO::getId),
            ExcelColumn.of("航次号", VoyageVO::getNo),
            ExcelColumn.of("船名", VoyageVO::getVslName),
            ExcelColumn.of("起始港口", VoyageVO::getLoadingPortName),
            ExcelColumn.of("目的港口", VoyageVO::getDischargePortName),
            ExcelColumn.of("录入时间", VoyageVO::getInsertTime),
            ExcelColumn.of("更新时间", VoyageVO::getUpdateTime)
    );

    @Operation(summary = "分页查询航次(可按航次号前缀、起始港口筛选)")
    @GetMapping
    public Result<PageResult<VoyageVO>> page(VoyageQueryReq req) {
        return Result.success(voyageService.page(req));
    }

    // ⚠️ /export 必须写在 /{id} **前面**。
    @Operation(summary = "导出航次为 Excel(筛选条件与列表一致, 不分页)")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(VoyageQueryReq req) {
        List<VoyageVO> rows = voyageService.listForExport(req);
        byte[] body = excelExporter.toXlsx("航次", EXPORT_COLUMNS, rows);
        return ExcelResponse.of("voyages", body);
    }

    @Operation(summary = "查询航次详情")
    @GetMapping("/{id}")
    public Result<VoyageVO> getById(@PathVariable Long id) {
        return Result.success(voyageService.getById(id));
    }

    @Operation(summary = "新增航次")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody VoyageReq req) {
        voyageService.create(req);
        return Result.success();
    }

    @Operation(summary = "修改航次")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody VoyageReq req) {
        voyageService.update(id, req);
        return Result.success();
    }

    @Operation(summary = "删除航次(有物流事件挂在该航次时拒绝)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        voyageService.delete(id);
        return Result.success();
    }
}
