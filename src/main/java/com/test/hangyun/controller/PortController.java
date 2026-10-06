package com.test.hangyun.controller;

import com.test.hangyun.common.Result;
import com.test.hangyun.dto.vo.PortOptionVO;
import com.test.hangyun.service.PortService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 港口。
 * <p>
 * 目前只有下拉框搜索一个接口: 新增/修改订单选起运港、目的港时用。
 * 完整的 /ports 资源(列表、详情、增删改)以后再做。
 */
@Tag(name = "港口", description = "港口下拉框搜索")
@RestController
@RequestMapping("/ports")
@RequiredArgsConstructor
public class PortController {

    private final PortService portService;

    @Operation(summary = "搜索港口(下拉框用: 中英文名或五字码前缀, 最多 20 条)")
    @GetMapping("/options")
    public Result<List<PortOptionVO>> options(
            @RequestParam(required = false) String keyword) {
        return Result.success(portService.options(keyword));
    }
}
