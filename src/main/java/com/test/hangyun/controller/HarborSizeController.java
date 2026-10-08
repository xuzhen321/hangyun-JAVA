package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.HarborSizeQueryReq;
import com.test.hangyun.dto.HarborSizeReq;
import com.test.hangyun.dto.vo.HarborSizeOptionVO;
import com.test.hangyun.dto.vo.HarborSizeVO;
import com.test.hangyun.service.HarborSizeService;
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

@Tag(name = "港口尺寸", description = "港口尺寸字典的增删改查")
@RestController
@RequestMapping("/harbor-sizes")
@RequiredArgsConstructor
public class HarborSizeController {

    private final HarborSizeService harborSizeService;

    @Operation(summary = "分页查询港口尺寸(可按尺寸前缀搜索)")
    @GetMapping
    public Result<PageResult<HarborSizeVO>> page(HarborSizeQueryReq req) {
        return Result.success(harborSizeService.page(req));
    }

    @Operation(summary = "港口尺寸下拉框(给新增港口选尺寸用, 一次性返回全部)")
    @GetMapping("/options")
    public Result<List<HarborSizeOptionVO>> options() {
        return Result.success(harborSizeService.options());
    }

    @Operation(summary = "查询港口尺寸详情")
    @GetMapping("/{id}")
    public Result<HarborSizeVO> getById(@PathVariable Long id) {
        return Result.success(harborSizeService.getById(id));
    }

    @Operation(summary = "新增港口尺寸")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody HarborSizeReq req) {
        harborSizeService.create(req);
        return Result.success();
    }

    @Operation(summary = "修改港口尺寸")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody HarborSizeReq req) {
        harborSizeService.update(id, req);
        return Result.success();
    }

    @Operation(summary = "删除港口尺寸(有港口在用该尺寸时拒绝)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        harborSizeService.delete(id);
        return Result.success();
    }
}
