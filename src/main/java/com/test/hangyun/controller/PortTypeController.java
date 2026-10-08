package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.PortTypeQueryReq;
import com.test.hangyun.dto.PortTypeReq;
import com.test.hangyun.dto.vo.PortTypeOptionVO;
import com.test.hangyun.dto.vo.PortTypeVO;
import com.test.hangyun.service.PortTypeService;
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

@Tag(name = "港口类型", description = "港口类型字典的增删改查")
@RestController
@RequestMapping("/port-types")
@RequiredArgsConstructor
public class PortTypeController {

    private final PortTypeService portTypeService;

    @Operation(summary = "分页查询港口类型(可按类型前缀搜索)")
    @GetMapping
    public Result<PageResult<PortTypeVO>> page(PortTypeQueryReq req) {
        return Result.success(portTypeService.page(req));
    }

    @Operation(summary = "港口类型下拉框(给新增港口选类型用, 一次性返回全部)")
    @GetMapping("/options")
    public Result<List<PortTypeOptionVO>> options() {
        return Result.success(portTypeService.options());
    }

    @Operation(summary = "查询港口类型详情")
    @GetMapping("/{id}")
    public Result<PortTypeVO> getById(@PathVariable Long id) {
        return Result.success(portTypeService.getById(id));
    }

    @Operation(summary = "新增港口类型")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody PortTypeReq req) {
        portTypeService.create(req);
        return Result.success();
    }

    @Operation(summary = "修改港口类型")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody PortTypeReq req) {
        portTypeService.update(id, req);
        return Result.success();
    }

    @Operation(summary = "删除港口类型(有港口在用该类型时拒绝)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        portTypeService.delete(id);
        return Result.success();
    }
}
