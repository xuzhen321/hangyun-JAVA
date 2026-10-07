package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.CargoTypeBatchDeleteReq;
import com.test.hangyun.dto.CargoTypeQueryReq;
import com.test.hangyun.dto.CargoTypeReq;
import com.test.hangyun.dto.vo.CargoTypeOptionVO;
import com.test.hangyun.dto.vo.CargoTypeVO;
import com.test.hangyun.service.CargoTypeService;
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

@Tag(name = "货物种类", description = "货物种类信息的增删改查")
@RestController
@RequestMapping("/cargo-types")
@RequiredArgsConstructor
public class CargoTypeController {

    private final CargoTypeService cargoTypeService;

    @Operation(summary = "分页查询货物种类(可按名称前缀筛选)")
    @GetMapping
    public Result<PageResult<CargoTypeVO>> page(CargoTypeQueryReq req) {
        return Result.success(cargoTypeService.page(req));
    }

    @Operation(summary = "货物种类下拉框(只返回 id/名称, 一次性全部返回)")
    @GetMapping("/options")
    public Result<List<CargoTypeOptionVO>> options(
            @RequestParam(required = false) String name) {
        return Result.success(cargoTypeService.options(name));
    }

    @Operation(summary = "查询货物种类详情")
    @GetMapping("/{id}")
    public Result<CargoTypeVO> getById(@PathVariable Long id) {
        return Result.success(cargoTypeService.getById(id));
    }

    @Operation(summary = "新增货物种类")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody CargoTypeReq req) {
        cargoTypeService.create(req);
        return Result.success();
    }

    @Operation(summary = "修改货物种类")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CargoTypeReq req) {
        cargoTypeService.update(id, req);
        return Result.success();
    }

    @Operation(summary = "批量删除货物种类(有一个被货物引用则整批拒绝)")
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@Valid @RequestBody CargoTypeBatchDeleteReq req) {
        cargoTypeService.deleteBatch(req.getIds());
        return Result.success();
    }

    @Operation(summary = "删除货物种类(有货物引用时拒绝)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        cargoTypeService.delete(id);
        return Result.success();
    }
}
