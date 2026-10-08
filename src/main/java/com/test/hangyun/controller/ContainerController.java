package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.ContainerBatchDeleteReq;
import com.test.hangyun.dto.ContainerCreateReq;
import com.test.hangyun.dto.ContainerQueryReq;
import com.test.hangyun.dto.ContainerUpdateReq;
import com.test.hangyun.dto.vo.ContainerVO;
import com.test.hangyun.service.ContainerService;
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
 * 集装箱管理。
 * <p>
 * 注意: 箱号是 {@code varchar(30)} 的字符串主键, 路径参数也是字符串, 不要当数字处理。
 */
@Tag(name = "集装箱", description = "集装箱信息的增删改查")
@RestController
@RequestMapping("/containers")
@RequiredArgsConstructor
public class ContainerController {

    private final ContainerService containerService;

    @Operation(summary = "分页查询集装箱(可按箱号前缀、状态精确筛选)")
    @GetMapping
    public Result<PageResult<ContainerVO>> page(ContainerQueryReq req) {
        return Result.success(containerService.page(req));
    }

    @Operation(summary = "查询集装箱详情")
    @GetMapping("/{no}")
    public Result<ContainerVO> getById(@PathVariable String no) {
        return Result.success(containerService.getById(no));
    }

    @Operation(summary = "新增集装箱(箱号由前端提供)")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ContainerCreateReq req) {
        containerService.create(req);
        return Result.success();
    }

    @Operation(summary = "修改集装箱(不能改箱号)")
    @PutMapping("/{no}")
    public Result<Void> update(@PathVariable String no,
                               @Valid @RequestBody ContainerUpdateReq req) {
        containerService.update(no, req);
        return Result.success();
    }

    @Operation(summary = "批量删除集装箱(有一个被装箱引用则整批拒绝)")
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@Valid @RequestBody ContainerBatchDeleteReq req) {
        containerService.deleteBatch(req.getNos());
        return Result.success();
    }

    @Operation(summary = "删除集装箱(被装箱结果引用时拒绝)")
    @DeleteMapping("/{no}")
    public Result<Void> delete(@PathVariable String no) {
        containerService.delete(no);
        return Result.success();
    }
}
