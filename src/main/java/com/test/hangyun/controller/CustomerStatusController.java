package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.CustomerStatusQueryReq;
import com.test.hangyun.dto.CustomerStatusReq;
import com.test.hangyun.dto.vo.CustomerStatusVO;
import com.test.hangyun.service.CustomerStatusService;
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

@Tag(name = "客户状态", description = "客户状态字典的增删改查")
@RestController
@RequestMapping("/customer-statuses")
@RequiredArgsConstructor
public class CustomerStatusController {

    private final CustomerStatusService customerStatusService;

    @Operation(summary = "分页查询客户状态")
    @GetMapping
    public Result<PageResult<CustomerStatusVO>> page(CustomerStatusQueryReq req) {
        return Result.success(customerStatusService.page(req));
    }

    @Operation(summary = "查询全部客户状态(供下拉框使用)")
    @GetMapping("/all")
    public Result<List<CustomerStatusVO>> all() {
        return Result.success(customerStatusService.listAll());
    }

    @Operation(summary = "查询客户状态详情")
    @GetMapping("/{id}")
    public Result<CustomerStatusVO> getById(@PathVariable Long id) {
        return Result.success(customerStatusService.getById(id));
    }

    @Operation(summary = "新增客户状态")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody CustomerStatusReq req) {
        customerStatusService.create(req);
        return Result.success();
    }

    @Operation(summary = "修改客户状态")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CustomerStatusReq req) {
        customerStatusService.update(id, req);
        return Result.success();
    }

    @Operation(summary = "删除客户状态(内置状态或被客户引用时拒绝)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        customerStatusService.delete(id);
        return Result.success();
    }
}
