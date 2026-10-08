package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.AreaQueryReq;
import com.test.hangyun.dto.AreaReq;
import com.test.hangyun.dto.vo.AreaOptionVO;
import com.test.hangyun.dto.vo.AreaVO;
import com.test.hangyun.service.AreaService;
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

@Tag(name = "区域字典", description = "区域字典的增删改查")
@RestController
@RequestMapping("/areas")
@RequiredArgsConstructor
public class AreaController {

    private final AreaService areaService;

    @Operation(summary = "分页查询区域(可按名称前缀搜索)")
    @GetMapping
    public Result<PageResult<AreaVO>> page(AreaQueryReq req) {
        return Result.success(areaService.page(req));
    }

    @Operation(summary = "区域下拉框(给新增港口选区域用, 一次性返回全部)")
    @GetMapping("/options")
    public Result<List<AreaOptionVO>> options() {
        return Result.success(areaService.options());
    }

    @Operation(summary = "查询区域详情")
    @GetMapping("/{id}")
    public Result<AreaVO> getById(@PathVariable Long id) {
        return Result.success(areaService.getById(id));
    }

    @Operation(summary = "新增区域")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody AreaReq req) {
        areaService.create(req);
        return Result.success();
    }

    @Operation(summary = "修改区域")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AreaReq req) {
        areaService.update(id, req);
        return Result.success();
    }

    @Operation(summary = "删除区域(有港口在用该区域时拒绝)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        areaService.delete(id);
        return Result.success();
    }
}
