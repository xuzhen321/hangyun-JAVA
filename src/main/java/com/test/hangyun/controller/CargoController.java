package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.CargoBatchDeleteReq;
import com.test.hangyun.dto.CargoCreateReq;
import com.test.hangyun.dto.CargoQueryReq;
import com.test.hangyun.dto.CargoUpdateReq;
import com.test.hangyun.dto.vo.CargoOptionVO;
import com.test.hangyun.dto.vo.CargoVO;
import com.test.hangyun.excel.ExcelColumn;
import com.test.hangyun.excel.ExcelExporter;
import com.test.hangyun.excel.ExcelResponse;
import com.test.hangyun.service.CargoService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "订单货物", description = "订单货物的增删改查")
@RestController
@RequestMapping("/cargos")
@RequiredArgsConstructor
public class CargoController {

    private final CargoService cargoService;

    private final ExcelExporter excelExporter;

    /** 导出列。**表头用中文**, 与列表列头一致 */
    private static final List<ExcelColumn<CargoVO>> EXPORT_COLUMNS = List.of(
            ExcelColumn.of("货物ID", CargoVO::getId),
            ExcelColumn.of("货物名称", CargoVO::getCargoTypeName),
            ExcelColumn.of("订单号", CargoVO::getOrderId),
            ExcelColumn.of("数量", CargoVO::getQuantity),
            ExcelColumn.of("录入时间", CargoVO::getInsertTime),
            ExcelColumn.of("更新时间", CargoVO::getUpdateTime)
    );

    @Operation(summary = "分页查询订单货物(可按货物名称前缀、订单号精确筛选)")
    @GetMapping
    public Result<PageResult<CargoVO>> page(CargoQueryReq req) {
        return Result.success(cargoService.page(req));
    }

    // ⚠️ /export 必须写在 /{id} **前面**。
    @Operation(summary = "导出订单货物为 Excel(筛选条件与列表一致, 不分页)")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(CargoQueryReq req) {
        List<CargoVO> rows = cargoService.listForExport(req);
        byte[] body = excelExporter.toXlsx("货物", EXPORT_COLUMNS, rows);
        return ExcelResponse.of("cargos", body);
    }

    @Operation(summary = "货物下拉框(给新增装箱结果选货物用: 名称前缀或订单号, 最多 20 条)")
    @GetMapping("/options")
    public Result<List<CargoOptionVO>> options(
            @RequestParam(required = false) String cargoTypeName,
            @RequestParam(required = false) String orderId) {
        return Result.success(cargoService.options(cargoTypeName, orderId));
    }

    @Operation(summary = "新增订单货物(订单号必填)")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody CargoCreateReq req) {
        cargoService.create(req);
        return Result.success();
    }

    @Operation(summary = "修改订单货物(只能改货物种类和数量, 不能改订单)")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CargoUpdateReq req) {
        cargoService.update(id, req);
        return Result.success();
    }

    @Operation(summary = "批量删除订单货物(有一条被装箱引用则整批拒绝)")
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@Valid @RequestBody CargoBatchDeleteReq req) {
        cargoService.deleteBatch(req.getIds());
        return Result.success();
    }

    @Operation(summary = "删除订单货物(被装箱结果引用时拒绝)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        cargoService.delete(id);
        return Result.success();
    }
}
