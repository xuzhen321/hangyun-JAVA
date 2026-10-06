package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.CustomerBatchDeleteReq;
import com.test.hangyun.dto.CustomerCreateReq;
import com.test.hangyun.dto.CustomerQueryReq;
import com.test.hangyun.dto.CustomerUpdateReq;
import com.test.hangyun.dto.vo.CustomerVO;
import com.test.hangyun.service.CustomerService;
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

@Tag(name = "客户管理", description = "客户的增删改查")
@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "分页查询客户")
    @GetMapping
    public Result<PageResult<CustomerVO>> page(CustomerQueryReq req) {
        return Result.success(customerService.page(req));
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
