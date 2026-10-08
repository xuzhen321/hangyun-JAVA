package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.TrailerCreateReq;
import com.test.hangyun.dto.TrailerQueryReq;
import com.test.hangyun.dto.TrailerUpdateReq;
import com.test.hangyun.dto.vo.TrailerOptionVO;
import com.test.hangyun.dto.vo.TrailerVO;
import com.test.hangyun.service.TrailerService;
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
 * 拖车管理。
 * <p>
 * 注意: 拖车号是 {@code varchar(30)} 的字符串主键(车牌号), 路径参数也是字符串。
 */
@Tag(name = "拖车管理", description = "拖车信息的增删改查")
@RestController
@RequestMapping("/trailers")
@RequiredArgsConstructor
public class TrailerController {

    private final TrailerService trailerService;

    @Operation(summary = "分页查询拖车(可按拖车号/司机姓名前缀搜索)")
    @GetMapping
    public Result<PageResult<TrailerVO>> page(TrailerQueryReq req) {
        return Result.success(trailerService.page(req));
    }

    @Operation(summary = "拖车下拉框(给新增提空箱记录选拖车用: 返回拖车号+司机姓名, 最多 20 条)")
    @GetMapping("/options")
    public Result<List<TrailerOptionVO>> options(
            @RequestParam(required = false) String keyword) {
        return Result.success(trailerService.options(keyword));
    }

    @Operation(summary = "查询拖车详情")
    @GetMapping("/{no}")
    public Result<TrailerVO> getById(@PathVariable String no) {
        return Result.success(trailerService.getById(no));
    }

    @Operation(summary = "新增拖车(拖车号由前端提供)")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody TrailerCreateReq req) {
        trailerService.create(req);
        return Result.success();
    }

    @Operation(summary = "修改拖车(不能改拖车号)")
    @PutMapping("/{no}")
    public Result<Void> update(@PathVariable String no,
                               @Valid @RequestBody TrailerUpdateReq req) {
        trailerService.update(no, req);
        return Result.success();
    }

    @Operation(summary = "删除拖车(被提空箱记录引用时拒绝)")
    @DeleteMapping("/{no}")
    public Result<Void> delete(@PathVariable String no) {
        trailerService.delete(no);
        return Result.success();
    }
}
