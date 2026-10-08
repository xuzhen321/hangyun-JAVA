package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.CountryQueryReq;
import com.test.hangyun.dto.CountryReq;
import com.test.hangyun.dto.vo.CountryOptionVO;
import com.test.hangyun.dto.vo.CountryVO;
import com.test.hangyun.service.CountryService;
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

@Tag(name = "国家字典", description = "国家字典的增删改查(船舶船旗国 / 港口所在国家共用)")
@RestController
@RequestMapping("/countries")
@RequiredArgsConstructor
public class CountryController {

    private final CountryService countryService;

    @Operation(summary = "分页查询国家(可按代码/中英文名前缀搜索)")
    @GetMapping
    public Result<PageResult<CountryVO>> page(CountryQueryReq req) {
        return Result.success(countryService.page(req));
    }

    @Operation(summary = "国家下拉框(给新增船舶选船旗国用: 返回中文名+代码, 最多 20 条)")
    @GetMapping("/options")
    public Result<List<CountryOptionVO>> options(
            @RequestParam(required = false) String keyword) {
        return Result.success(countryService.options(keyword));
    }

    @Operation(summary = "查询国家详情")
    @GetMapping("/{id}")
    public Result<CountryVO> getById(@PathVariable Long id) {
        return Result.success(countryService.getById(id));
    }

    @Operation(summary = "新增国家")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody CountryReq req) {
        countryService.create(req);
        return Result.success();
    }

    @Operation(summary = "修改国家")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CountryReq req) {
        countryService.update(id, req);
        return Result.success();
    }

    @Operation(summary = "删除国家(被船舶或港口引用时拒绝)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        countryService.delete(id);
        return Result.success();
    }
}
