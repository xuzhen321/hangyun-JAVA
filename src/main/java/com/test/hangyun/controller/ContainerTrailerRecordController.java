package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.ContainerTrailerRecordBatchDeleteReq;
import com.test.hangyun.dto.ContainerTrailerRecordQueryReq;
import com.test.hangyun.dto.ContainerTrailerRecordReq;
import com.test.hangyun.dto.vo.ContainerTrailerRecordVO;
import com.test.hangyun.service.ContainerTrailerRecordService;
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

@Tag(name = "提空箱登记", description = "集装箱拖车记录的增删改查")
@RestController
@RequestMapping("/container-trailer-records")
@RequiredArgsConstructor
public class ContainerTrailerRecordController {

    private final ContainerTrailerRecordService recordService;

    @Operation(summary = "分页查询提空箱记录(可按箱号/拖车号前缀、进场时间区间筛选)")
    @GetMapping
    public Result<PageResult<ContainerTrailerRecordVO>> page(ContainerTrailerRecordQueryReq req) {
        return Result.success(recordService.page(req));
    }

    @Operation(summary = "查询提空箱记录详情")
    @GetMapping("/{id}")
    public Result<ContainerTrailerRecordVO> getById(@PathVariable Long id) {
        return Result.success(recordService.getById(id));
    }

    @Operation(summary = "新增提空箱登记(箱号/拖车号都必填)")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ContainerTrailerRecordReq req) {
        recordService.create(req);
        return Result.success();
    }

    @Operation(summary = "修改提空箱记录")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @Valid @RequestBody ContainerTrailerRecordReq req) {
        recordService.update(id, req);
        return Result.success();
    }

    @Operation(summary = "批量删除提空箱记录(物理删除)")
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@Valid @RequestBody ContainerTrailerRecordBatchDeleteReq req) {
        recordService.deleteBatch(req.getIds());
        return Result.success();
    }

    @Operation(summary = "删除提空箱记录(物理删除)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        recordService.delete(id);
        return Result.success();
    }
}
