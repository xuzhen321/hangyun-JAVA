package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.CustomerBatchDeleteReq;
import com.test.hangyun.dto.CustomerCreateReq;
import com.test.hangyun.dto.CustomerOrderQueryReq;
import com.test.hangyun.dto.CustomerQueryReq;
import com.test.hangyun.dto.CustomerUpdateReq;
import com.test.hangyun.dto.vo.CustomerOptionVO;
import com.test.hangyun.dto.vo.CustomerVO;
import com.test.hangyun.dto.vo.OrderVO;
import com.test.hangyun.service.CustomerService;
import com.test.hangyun.service.OrderService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "客户管理", description = "客户的增删改查")
@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    /**
     * 客户详情页要展示他名下的订单, 所以这里跨模块用了 OrderService。
     * 之所以挂在 /customers/{id}/orders 而不是 /orders 下面, 是因为"客户的订单"是客户的子资源,
     * 前端在客户详情页手上正好有客户 id。
     */
    private final OrderService orderService;

    @Operation(summary = "分页查询客户")
    @GetMapping
    public Result<PageResult<CustomerVO>> page(CustomerQueryReq req) {
        return Result.success(customerService.page(req));
    }

    @Operation(summary = "搜索客户(下拉框用: 姓名前缀, 最多 20 条, 只返回 id/姓名/电话)")
    @GetMapping("/options")
    public Result<List<CustomerOptionVO>> options(
            @RequestParam(required = false) String name) {
        return Result.success(customerService.options(name));
    }

    @Operation(summary = "查询某个客户名下的订单(分页, 含已取消)")
    @GetMapping("/{id}/orders")
    public Result<PageResult<OrderVO>> orders(@PathVariable Long id, CustomerOrderQueryReq req) {
        return Result.success(orderService.pageByCustomer(id, req));
    }

    @Operation(summary = "查询客户详情")
    @GetMapping("/{id}")
    public Result<CustomerVO> getById(@PathVariable Long id) {
        return Result.success(customerService.getById(id));
    }

    @Operation(summary = "新增客户")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody CustomerCreateReq req) {
        customerService.create(req);
        return Result.success();
    }

    @Operation(summary = "修改客户")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CustomerUpdateReq req) {
        customerService.update(id, req);
        return Result.success();
    }

    @Operation(summary = "批量删除客户(逻辑删除, 不存在的 id 忽略)")
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@Valid @RequestBody CustomerBatchDeleteReq req) {
        customerService.deleteBatch(req.getIds());
        return Result.success();
    }

    @Operation(summary = "删除客户(逻辑删除, 状态改为注销)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return Result.success();
    }
}
