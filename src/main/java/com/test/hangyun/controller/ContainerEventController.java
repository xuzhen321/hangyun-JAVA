package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.ContainerEventBatchDeleteReq;
import com.test.hangyun.dto.ContainerEventQueryReq;
import com.test.hangyun.dto.ContainerEventReq;
import com.test.hangyun.dto.vo.ContainerEventVO;
import com.test.hangyun.service.ContainerEventService;
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

/**
 * 集装箱物流事件。
 * <p>
 * 集装箱的**轨迹接口**（{@code GET /containers/{no}/track}）和概览在
 * {@link ContainerController} 里 —— 那两条路径挂在 /containers 下面。
 */
@Tag(name = "物流事件", description = "集装箱物流事件的增删改查")
@RestController
@RequestMapping("/container-events")
@RequiredArgsConstructor
public class ContainerEventController {

    private final ContainerEventService containerEventService;

    @Operation(summary = "分页查询物流事件(可按箱号精确、状态精确、发生时间区间筛选)")
    @GetMapping
    public Result<PageResult<ContainerEventVO>> page(ContainerEventQueryReq req) {
        return Result.success(containerEventService.page(req));
    }

    @Operation(summary = "查询物流事件详情")
    @GetMapping("/{id}")
    public Result<ContainerEventVO> getById(@PathVariable Long id) {
        return Result.success(containerEventService.getById(id));
    }

    @Operation(summary = "新增物流事件")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ContainerEventReq req) {
        containerEventService.create(req);
        return Result.success();
    }

    @Operation(summary = "修改物流事件")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ContainerEventReq req) {
        containerEventService.update(id, req);
        return Result.success();
    }

    @Operation(summary = "批量删除物流事件(逻辑删除, 状态改为已删除)")
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@Valid @RequestBody ContainerEventBatchDeleteReq req) {
        containerEventService.deleteBatch(req.getIds());
        return Result.success();
    }

    @Operation(summary = "删除物流事件(逻辑删除, 状态改为已删除)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        containerEventService.delete(id);
        return Result.success();
    }
}
