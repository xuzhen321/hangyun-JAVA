package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.PortCreateReq;
import com.test.hangyun.dto.PortQueryReq;
import com.test.hangyun.dto.PortUpdateReq;
import com.test.hangyun.dto.vo.PortOptionVO;
import com.test.hangyun.dto.vo.PortVO;
import com.test.hangyun.service.PortService;
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

/**
 * 港口。
 * <p>
 * 删除是**逻辑删除**(state 置 '3'), 列表默认排除已删除的; state 不接受前端传入。
 */
@Tag(name = "港口管理", description = "港口的增删改查")
@RestController
@RequestMapping("/ports")
@RequiredArgsConstructor
public class PortController {

    private final PortService portService;

    @Operation(summary = "分页查询港口(可按五字码/中英文名前缀搜索, 按国家筛选)")
    @GetMapping
    public Result<PageResult<PortVO>> page(PortQueryReq req) {
        return Result.success(portService.page(req));
    }

    @Operation(summary = "搜索港口(下拉框用: 中英文名或五字码前缀, 最多 20 条)")
    @GetMapping("/options")
    public Result<List<PortOptionVO>> options(
            @RequestParam(required = false) String keyword) {
        return Result.success(portService.options(keyword));
    }

    @Operation(summary = "查询港口详情")
    @GetMapping("/{id}")
    public Result<PortVO> getById(@PathVariable Long id) {
        return Result.success(portService.getById(id));
    }

    @Operation(summary = "新增港口")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody PortCreateReq req) {
        portService.create(req);
        return Result.success();
    }

    @Operation(summary = "修改港口")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody PortUpdateReq req) {
        portService.update(id, req);
        return Result.success();
    }

    @Operation(summary = "删除港口(逻辑删除, 状态置为已删除)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        portService.delete(id);
        return Result.success();
    }
}
