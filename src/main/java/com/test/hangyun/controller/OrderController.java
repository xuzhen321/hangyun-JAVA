package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.OrderBatchDeleteReq;
import com.test.hangyun.dto.OrderCreateReq;
import com.test.hangyun.dto.OrderQueryReq;
import com.test.hangyun.dto.OrderUpdateReq;
import com.test.hangyun.dto.vo.OrderVO;
import com.test.hangyun.excel.ExcelColumn;
import com.test.hangyun.excel.ExcelExporter;
import com.test.hangyun.excel.ExcelResponse;
import com.test.hangyun.service.OrderService;
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
 * 订单管理。
 * <p>
 * 注意: 订单号是 {@code varchar(50)} 的字符串, 路径参数也是字符串, 不要当数字处理。
 */
@Tag(name = "订单管理", description = "订单的增删改查")
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    private final ExcelExporter excelExporter;

    /** 导出列。**表头用中文**, 状态导中文描述而不是状态 id */
    private static final List<ExcelColumn<OrderVO>> EXPORT_COLUMNS = List.of(
            ExcelColumn.of("订单号", OrderVO::getId),
            ExcelColumn.of("客户姓名", OrderVO::getCustomerName),
            ExcelColumn.of("下单时间", OrderVO::getOrderDate),
            ExcelColumn.of("起运港", OrderVO::getLoadingPortName),
            ExcelColumn.of("目的港", OrderVO::getDischargePortName),
            ExcelColumn.of("状态", OrderVO::getStatusDescription),
            ExcelColumn.of("录入时间", OrderVO::getInsertTime),
            ExcelColumn.of("更新时间", OrderVO::getUpdateTime)
    );

    @Operation(summary = "分页查询订单")
    @GetMapping
    public Result<PageResult<OrderVO>> page(OrderQueryReq req) {
        return Result.success(orderService.page(req));
    }

    // ⚠️ /export 必须写在 /{id} **前面**。
    @Operation(summary = "导出订单为 Excel(筛选条件与列表一致, 不分页)")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(OrderQueryReq req) {
        List<OrderVO> rows = orderService.listForExport(req);
        byte[] body = excelExporter.toXlsx("订单", EXPORT_COLUMNS, rows);
        return ExcelResponse.of("orders", body);
    }

    @Operation(summary = "查询订单详情")
    @GetMapping("/{id}")
    public Result<OrderVO> getById(@PathVariable String id) {
        return Result.success(orderService.getById(id));
    }

    @Operation(summary = "新增订单(订单号由后端生成)")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody OrderCreateReq req) {
        orderService.create(req);
        return Result.success();
    }

    @Operation(summary = "修改订单")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable String id, @Valid @RequestBody OrderUpdateReq req) {
        orderService.update(id, req);
        return Result.success();
    }

    @Operation(summary = "批量删除订单(逻辑删除改为已取消, 不存在的订单号忽略)")
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@Valid @RequestBody OrderBatchDeleteReq req) {
        orderService.deleteBatch(req.getIds());
        return Result.success();
    }

    @Operation(summary = "删除订单(逻辑删除, 状态改为已取消)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        orderService.delete(id);
        return Result.success();
    }
}
