package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.VoyageQueryReq;
import com.test.hangyun.dto.VoyageReq;
import com.test.hangyun.dto.vo.VoyageVO;
import com.test.hangyun.service.VoyageService;
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

@Tag(name = "航次管理", description = "航次信息的增删改查")
@RestController
@RequestMapping("/voyages")
@RequiredArgsConstructor
public class VoyageController {

    private final VoyageService voyageService;

    @Operation(summary = "分页查询航次(可按航次号前缀、起始港口筛选)")
    @GetMapping
    public Result<PageResult<VoyageVO>> page(VoyageQueryReq req) {
        return Result.success(voyageService.page(req));
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
