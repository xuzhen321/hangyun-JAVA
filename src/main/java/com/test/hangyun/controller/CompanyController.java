package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.CompanyQueryReq;
import com.test.hangyun.dto.CompanyReq;
import com.test.hangyun.dto.vo.CompanyOptionVO;
import com.test.hangyun.dto.vo.CompanyVO;
import com.test.hangyun.service.CompanyService;
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

@Tag(name = "公司信息", description = "公司的增删改查(箱主 / 操作方 / 船东 / 管理公司共用)")
@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @Operation(summary = "分页查询公司(可按名称/代码前缀搜索)")
    @GetMapping
    public Result<PageResult<CompanyVO>> page(CompanyQueryReq req) {
        return Result.success(companyService.page(req));
    }

    @Operation(summary = "公司下拉框(给新增集装箱选箱主/操作方用: 名称或代码前缀搜索, 返回名称+代码)")
    @GetMapping("/options")
    public Result<List<CompanyOptionVO>> options(
            @RequestParam(required = false) String keyword) {
        return Result.success(companyService.options(keyword));
    }

    @Operation(summary = "查询公司详情")
    @GetMapping("/{id}")
    public Result<CompanyVO> getById(@PathVariable Long id) {
        return Result.success(companyService.getById(id));
    }

    @Operation(summary = "新增公司")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody CompanyReq req) {
        companyService.create(req);
        return Result.success();
    }

    @Operation(summary = "修改公司")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CompanyReq req) {
        companyService.update(id, req);
        return Result.success();
    }

    @Operation(summary = "删除公司(被集装箱引用为箱主或操作方时拒绝)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        companyService.delete(id);
        return Result.success();
    }
}
