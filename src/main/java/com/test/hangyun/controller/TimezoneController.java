package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.TimezoneQueryReq;
import com.test.hangyun.dto.TimezoneReq;
import com.test.hangyun.dto.vo.TimezoneOptionVO;
import com.test.hangyun.dto.vo.TimezoneVO;
import com.test.hangyun.service.TimezoneService;
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

@Tag(name = "时区字典", description = "时区字典的增删改查")
@RestController
@RequestMapping("/timezones")
@RequiredArgsConstructor
public class TimezoneController {

    private final TimezoneService timezoneService;

    @Operation(summary = "分页查询时区(可按任一时区字段前缀搜索)")
    @GetMapping
    public Result<PageResult<TimezoneVO>> page(TimezoneQueryReq req) {
        return Result.success(timezoneService.page(req));
    }

    @Operation(summary = "时区下拉框(给新增港口选时区用, 一次性返回全部)")
    @GetMapping("/options")
    public Result<List<TimezoneOptionVO>> options() {
        return Result.success(timezoneService.options());
    }

    @Operation(summary = "查询时区详情")
    @GetMapping("/{id}")
    public Result<TimezoneVO> getById(@PathVariable Long id) {
        return Result.success(timezoneService.getById(id));
    }

    @Operation(summary = "新增时区")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody TimezoneReq req) {
        timezoneService.create(req);
        return Result.success();
    }

    @Operation(summary = "修改时区")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TimezoneReq req) {
        timezoneService.update(id, req);
        return Result.success();
    }

    @Operation(summary = "删除时区(有港口在用该时区时拒绝)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        timezoneService.delete(id);
        return Result.success();
    }
}
