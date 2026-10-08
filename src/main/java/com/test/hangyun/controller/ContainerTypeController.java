package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.ContainerTypeQueryReq;
import com.test.hangyun.dto.ContainerTypeReq;
import com.test.hangyun.dto.vo.ContainerTypeOptionVO;
import com.test.hangyun.dto.vo.ContainerTypeVO;
import com.test.hangyun.service.ContainerTypeService;
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

@Tag(name = "集装箱箱型", description = "集装箱箱型字典的增删改查")
@RestController
@RequestMapping("/container-types")
@RequiredArgsConstructor
public class ContainerTypeController {

    private final ContainerTypeService containerTypeService;

    @Operation(summary = "分页查询箱型")
    @GetMapping
    public Result<PageResult<ContainerTypeVO>> page(ContainerTypeQueryReq req) {
        return Result.success(containerTypeService.page(req));
    }

    @Operation(summary = "箱型下拉框(给新增集装箱选箱型用, 一次性返回全部)")
    @GetMapping("/options")
    public Result<List<ContainerTypeOptionVO>> options() {
        return Result.success(containerTypeService.options());
    }

    @Operation(summary = "查询箱型详情")
    @GetMapping("/{id}")
    public Result<ContainerTypeVO> getById(@PathVariable Long id) {
        return Result.success(containerTypeService.getById(id));
    }

    @Operation(summary = "新增箱型")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ContainerTypeReq req) {
        containerTypeService.create(req);
        return Result.success();
    }

    @Operation(summary = "修改箱型")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ContainerTypeReq req) {
        containerTypeService.update(id, req);
        return Result.success();
    }

    @Operation(summary = "删除箱型(有集装箱在用该箱型时拒绝)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        containerTypeService.delete(id);
        return Result.success();
    }
}
