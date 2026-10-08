package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.ContainerStatusQueryReq;
import com.test.hangyun.dto.ContainerStatusReq;
import com.test.hangyun.dto.vo.ContainerStatusOptionVO;
import com.test.hangyun.dto.vo.ContainerStatusVO;
import com.test.hangyun.service.ContainerStatusService;
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

@Tag(name = "集装箱状态", description = "集装箱状态字典的增删改查")
@RestController
@RequestMapping("/container-statuses")
@RequiredArgsConstructor
public class ContainerStatusController {

    private final ContainerStatusService containerStatusService;

    @Operation(summary = "分页查询集装箱状态")
    @GetMapping
    public Result<PageResult<ContainerStatusVO>> page(ContainerStatusQueryReq req) {
        return Result.success(containerStatusService.page(req));
    }

    @Operation(summary = "集装箱状态下拉框(给新增集装箱选状态用, 一次性返回全部)")
    @GetMapping("/options")
    public Result<List<ContainerStatusOptionVO>> options() {
        return Result.success(containerStatusService.options());
    }

    @Operation(summary = "查询集装箱状态详情")
    @GetMapping("/{id}")
    public Result<ContainerStatusVO> getById(@PathVariable Long id) {
        return Result.success(containerStatusService.getById(id));
    }

    @Operation(summary = "新增集装箱状态")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ContainerStatusReq req) {
        containerStatusService.create(req);
        return Result.success();
    }

    @Operation(summary = "修改集装箱状态")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ContainerStatusReq req) {
        containerStatusService.update(id, req);
        return Result.success();
    }

    @Operation(summary = "删除集装箱状态(有集装箱在用该状态时拒绝)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        containerStatusService.delete(id);
        return Result.success();
    }
}
