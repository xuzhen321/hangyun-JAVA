package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.CargoContainerResultBatchDeleteReq;
import com.test.hangyun.dto.CargoContainerResultQueryReq;
import com.test.hangyun.dto.CargoContainerResultReq;
import com.test.hangyun.dto.vo.CargoContainerResultVO;
import com.test.hangyun.excel.ExcelColumn;
import com.test.hangyun.excel.ExcelExporter;
import com.test.hangyun.excel.ExcelResponse;
import com.test.hangyun.service.CargoContainerResultService;
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

@Tag(name = "货物装箱结果", description = "货物装箱结果的增删改查")
@RestController
@RequestMapping("/cargo-container-results")
@RequiredArgsConstructor
public class CargoContainerResultController {

    private final CargoContainerResultService cargoContainerResultService;

    private final ExcelExporter excelExporter;

    /** 导出列。**表头用中文**, 与列表列头一致 */
    private static final List<ExcelColumn<CargoContainerResultVO>> EXPORT_COLUMNS = List.of(
            ExcelColumn.of("记录ID", CargoContainerResultVO::getId),
            ExcelColumn.of("货物名称", CargoContainerResultVO::getCargoTypeName),
            ExcelColumn.of("订单号", CargoContainerResultVO::getOrderId),
            ExcelColumn.of("集装箱号", CargoContainerResultVO::getContainerNo),
            ExcelColumn.of("装箱数量", CargoContainerResultVO::getQuantity),
            ExcelColumn.of("录入时间", CargoContainerResultVO::getInsertTime),
            ExcelColumn.of("更新时间", CargoContainerResultVO::getUpdateTime)
    );

    @Operation(summary = "分页查询装箱结果(可按货物名称前缀、订单号精确、集装箱号精确筛选)")
    @GetMapping
    public Result<PageResult<CargoContainerResultVO>> page(CargoContainerResultQueryReq req) {
        return Result.success(cargoContainerResultService.page(req));
    }

    // ⚠️ /export 必须写在 /{id} **前面**。
    @Operation(summary = "导出装箱结果为 Excel(筛选条件与列表一致, 不分页)")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(CargoContainerResultQueryReq req) {
        List<CargoContainerResultVO> rows = cargoContainerResultService.listForExport(req);
        byte[] body = excelExporter.toXlsx("装箱结果", EXPORT_COLUMNS, rows);
        return ExcelResponse.of("cargo-container-results", body);
    }

    @Operation(summary = "查询装箱结果详情")
    @GetMapping("/{id}")
    public Result<CargoContainerResultVO> getById(@PathVariable Long id) {
        return Result.success(cargoContainerResultService.getById(id));
    }

    @Operation(summary = "新增装箱结果(货物/箱号/数量都必填)")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody CargoContainerResultReq req) {
        cargoContainerResultService.create(req);
        return Result.success();
    }

    @Operation(summary = "修改装箱结果(三个字段整体覆盖)")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @Valid @RequestBody CargoContainerResultReq req) {
        cargoContainerResultService.update(id, req);
        return Result.success();
    }

    @Operation(summary = "批量删除装箱结果(物理删除)")
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@Valid @RequestBody CargoContainerResultBatchDeleteReq req) {
        cargoContainerResultService.deleteBatch(req.getIds());
        return Result.success();
    }

    @Operation(summary = "删除装箱结果(物理删除)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        cargoContainerResultService.delete(id);
        return Result.success();
    }
}
