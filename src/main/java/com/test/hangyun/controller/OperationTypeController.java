package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.OperationTypeQueryReq;
import com.test.hangyun.dto.OperationTypeReq;
import com.test.hangyun.dto.vo.OperationTypeOptionVO;
import com.test.hangyun.dto.vo.OperationTypeVO;
import com.test.hangyun.service.OperationTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 操作类型字典。
 * <p>
 * ⚠️ **只有查询和修改, 没有新增和删除** —— 这是一张**固定的字典**, 五行就是全部:
 * INSERT(1) / UPDATE(2) / DELETE(3) / EXPORT(4) / LOGIN(5)。
 * 代码里的 {@code @OpLog} 按 id 引用它们, 删一行会让新日志的类型列空白,
 * 加一行则没有任何代码会用上。数据由 {@code operation-type-data.sql} 初始化。
 */
@Tag(name = "操作类型字典", description = "操作日志的类型字典（只读 + 修改，不支持增删）")
@RestController
@RequestMapping("/operation-types")
@RequiredArgsConstructor
public class OperationTypeController {

    private final OperationTypeService operationTypeService;

    @Operation(summary = "分页查询操作类型(可按名称前缀搜索)")
    @GetMapping
    public Result<PageResult<OperationTypeVO>> page(OperationTypeQueryReq req) {
        return Result.success(operationTypeService.page(req));
    }

    @Operation(summary = "操作类型下拉框(一次性返回全部)")
    @GetMapping("/options")
    public Result<List<OperationTypeOptionVO>> options() {
        return Result.success(operationTypeService.options());
    }

    @Operation(summary = "查询操作类型详情")
    @GetMapping("/{id}")
    public Result<OperationTypeVO> getById(@PathVariable Long id) {
        return Result.success(operationTypeService.getById(id));
    }

    @Operation(summary = "修改操作类型名称(只改展示文字, 不影响日志里已有的记录)")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody OperationTypeReq req) {
        operationTypeService.update(id, req);
        return Result.success();
    }
}
