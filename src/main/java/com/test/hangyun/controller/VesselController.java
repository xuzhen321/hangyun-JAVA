package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.VesselBatchDeleteReq;
import com.test.hangyun.dto.VesselCreateReq;
import com.test.hangyun.dto.VesselQueryReq;
import com.test.hangyun.dto.VesselUpdateReq;
import com.test.hangyun.dto.vo.VesselOptionVO;
import com.test.hangyun.dto.vo.VesselVO;
import com.test.hangyun.service.VesselService;
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

@Tag(name = "船舶管理", description = "船舶信息的增删改查")
@RestController
@RequestMapping("/vessels")
@RequiredArgsConstructor
public class VesselController {

    private final VesselService vesselService;

    @Operation(summary = "分页查询船舶(可按船名/MMSI/IMO前缀搜索, 按船旗国/船型筛选)")
    @GetMapping
    public Result<PageResult<VesselVO>> page(VesselQueryReq req) {
        return Result.success(vesselService.page(req));
    }

    @Operation(summary = "船舶下拉框(给新增航次选船用: 船名/MMSI前缀, 返回船名+MMSI)")
    @GetMapping("/options")
    public Result<List<VesselOptionVO>> options(
            @RequestParam(required = false) String keyword) {
        return Result.success(vesselService.options(keyword));
    }

    @Operation(summary = "查询船舶详情")
    @GetMapping("/{id}")
    public Result<VesselVO> getById(@PathVariable Long id) {
        return Result.success(vesselService.getById(id));
    }

    @Operation(summary = "新增船舶")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody VesselCreateReq req) {
        vesselService.create(req);
        return Result.success();
    }

    @Operation(summary = "修改船舶")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody VesselUpdateReq req) {
        vesselService.update(id, req);
        return Result.success();
    }

    @Operation(summary = "批量删除船舶(有一艘被航次引用则整批拒绝)")
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@Valid @RequestBody VesselBatchDeleteReq req) {
        vesselService.deleteBatch(req.getIds());
        return Result.success();
    }

    @Operation(summary = "删除船舶(被航次引用时拒绝)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        vesselService.delete(id);
        return Result.success();
    }
}
