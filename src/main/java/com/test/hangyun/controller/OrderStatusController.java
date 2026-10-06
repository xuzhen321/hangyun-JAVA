package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.OrderStatusQueryReq;
import com.test.hangyun.dto.OrderStatusReq;
import com.test.hangyun.dto.vo.OrderStatusVO;
import com.test.hangyun.service.OrderStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "订单状态", description = "订单状态字典的增删改查")
@RestController
@RequestMapping("/order-statuses")
@RequiredArgsConstructor
public class OrderStatusController {

    private final OrderStatusService orderStatusService;

    @Operation(summary = "分页查询订单状态")
    @GetMapping
    public Result<PageResult<OrderStatusVO>> page(OrderStatusQueryReq req) {
        return Result.success(orderStatusService.page(req));
    }

    @Operation(summary = "查询全部订单状态(供下拉框使用)")
    @GetMapping("/all")
    public Result<List<OrderStatusVO>> all() {
        return Result.success(orderStatusService.listAll());
    }

    @Operation(summary = "查询订单状态详情")
    @GetMapping("/{id}")
    public Result<OrderStatusVO> getById(@PathVariable Long id) {
        return Result.success(orderStatusService.getById(id));
    }

    @Operation(summary = "新增订单状态")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody OrderStatusReq req) {
        orderStatusService.create(req);
        return Result.success();
    }

    @Operation(summary = "修改订单状态")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody OrderStatusReq req) {
        orderStatusService.update(id, req);
        return Result.success();
    }

    @Operation(summary = "删除订单状态(内置状态或被订单引用时拒绝)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        orderStatusService.delete(id);
        return Result.success();
    }
}
