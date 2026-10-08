package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.PortLevelQueryReq;
import com.test.hangyun.dto.PortLevelReq;
import com.test.hangyun.dto.vo.PortLevelOptionVO;
import com.test.hangyun.dto.vo.PortLevelVO;
import com.test.hangyun.service.PortLevelService;
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

@Tag(name = "港口级别", description = "港口级别字典的增删改查")
@RestController
@RequestMapping("/port-levels")
@RequiredArgsConstructor
public class PortLevelController {

    private final PortLevelService portLevelService;

    @Operation(summary = "分页查询港口级别")
    @GetMapping
    public Result<PageResult<PortLevelVO>> page(PortLevelQueryReq req) {
        return Result.success(portLevelService.page(req));
    }

    @Operation(summary = "港口级别下拉框(给新增港口选级别用, 一次性返回全部)")
    @GetMapping("/options")
    public Result<List<PortLevelOptionVO>> options() {
        return Result.success(portLevelService.options());
    }

    @Operation(summary = "查询港口级别详情")
    @GetMapping("/{id}")
    public Result<PortLevelVO> getById(@PathVariable Long id) {
        return Result.success(portLevelService.getById(id));
    }

    @Operation(summary = "新增港口级别")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody PortLevelReq req) {
        portLevelService.create(req);
        return Result.success();
    }

    @Operation(summary = "修改港口级别")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody PortLevelReq req) {
        portLevelService.update(id, req);
        return Result.success();
    }

    @Operation(summary = "删除港口级别(有港口在用该级别时拒绝)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        portLevelService.delete(id);
        return Result.success();
    }
}
