package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.ShipTypeQueryReq;
import com.test.hangyun.dto.ShipTypeReq;
import com.test.hangyun.dto.vo.ShipTypeOptionVO;
import com.test.hangyun.dto.vo.ShipTypeVO;
import com.test.hangyun.service.ShipTypeService;
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

@Tag(name = "船舶类型", description = "船舶类型字典的增删改查")
@RestController
@RequestMapping("/ship-types")
@RequiredArgsConstructor
public class ShipTypeController {

    private final ShipTypeService shipTypeService;

    @Operation(summary = "分页查询船舶类型(可按名称前缀搜索)")
    @GetMapping
    public Result<PageResult<ShipTypeVO>> page(ShipTypeQueryReq req) {
        return Result.success(shipTypeService.page(req));
    }

    @Operation(summary = "船舶类型下拉框(给新增船舶选船型用, 一次性返回全部)")
    @GetMapping("/options")
    public Result<List<ShipTypeOptionVO>> options() {
        return Result.success(shipTypeService.options());
    }

    @Operation(summary = "查询船舶类型详情")
    @GetMapping("/{id}")
    public Result<ShipTypeVO> getById(@PathVariable Long id) {
        return Result.success(shipTypeService.getById(id));
    }

    @Operation(summary = "新增船舶类型")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ShipTypeReq req) {
        shipTypeService.create(req);
        return Result.success();
    }

    @Operation(summary = "修改船舶类型")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ShipTypeReq req) {
        shipTypeService.update(id, req);
        return Result.success();
    }

    @Operation(summary = "删除船舶类型(有船舶在用该船型时拒绝)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        shipTypeService.delete(id);
        return Result.success();
    }
}
