package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.EventStatusQueryReq;
import com.test.hangyun.dto.EventStatusReq;
import com.test.hangyun.dto.vo.EventStatusOptionVO;
import com.test.hangyun.dto.vo.EventStatusVO;
import com.test.hangyun.service.EventStatusService;
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

@Tag(name = "事件状态", description = "集装箱事件状态字典的增删改查")
@RestController
@RequestMapping("/event-statuses")
@RequiredArgsConstructor
public class EventStatusController {

    private final EventStatusService eventStatusService;

    @Operation(summary = "分页查询事件状态")
    @GetMapping
    public Result<PageResult<EventStatusVO>> page(EventStatusQueryReq req) {
        return Result.success(eventStatusService.page(req));
    }

    @Operation(summary = "事件状态下拉框(给新增物流事件选状态用, 一次性返回全部)")
    @GetMapping("/options")
    public Result<List<EventStatusOptionVO>> options() {
        return Result.success(eventStatusService.options());
    }

    @Operation(summary = "查询事件状态详情")
    @GetMapping("/{id}")
    public Result<EventStatusVO> getById(@PathVariable Long id) {
        return Result.success(eventStatusService.getById(id));
    }

    @Operation(summary = "新增事件状态")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody EventStatusReq req) {
        eventStatusService.create(req);
        return Result.success();
    }

    @Operation(summary = "修改事件状态")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody EventStatusReq req) {
        eventStatusService.update(id, req);
        return Result.success();
    }

    @Operation(summary = "删除事件状态(有物流事件在用该状态时拒绝)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        eventStatusService.delete(id);
        return Result.success();
    }
}
