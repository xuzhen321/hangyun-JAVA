package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.ContainerBatchDeleteReq;
import com.test.hangyun.dto.ContainerCreateReq;
import com.test.hangyun.dto.ContainerQueryReq;
import com.test.hangyun.dto.ContainerUpdateReq;
import com.test.hangyun.dto.vo.ContainerEventVO;
import com.test.hangyun.dto.vo.ContainerOptionVO;
import com.test.hangyun.dto.vo.ContainerSummaryVO;
import com.test.hangyun.dto.vo.ContainerVO;
import com.test.hangyun.excel.ExcelColumn;
import com.test.hangyun.excel.ExcelExporter;
import com.test.hangyun.excel.ExcelResponse;
import com.test.hangyun.service.ContainerEventService;
import com.test.hangyun.service.ContainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    /**
     * 轨迹和概览挂在 /containers/{no} 下面(它们是"某个集装箱的物流信息"), 所以这里跨模块用了
     * ContainerEventService。和 CustomerController 用 OrderService 是同一个理由。
     */
    private final ContainerEventService containerEventService;

    private final ExcelExporter excelExporter;

    /**
     * 导出列。**表头用中文**, 状态导中文描述而不是状态 id,
     * 两个标志位导"是"/"否"而不是 true/false。
     */
    private static final List<ExcelColumn<ContainerVO>> EXPORT_COLUMNS = List.of(
            ExcelColumn.of("箱号", ContainerVO::getNo),
            ExcelColumn.of("箱型", ContainerVO::getTypeName),
            ExcelColumn.of("箱尺寸", ContainerVO::getTypeSize),
            ExcelColumn.of("箱主", ContainerVO::getOwnerName),
            ExcelColumn.of("操作方", ContainerVO::getOperatorName),
            ExcelColumn.of("封号", ContainerVO::getSealNo),
            ExcelColumn.of("状态", ContainerVO::getStatusDescription),
            ExcelColumn.of("危险品", vo -> flagText(vo.getDangerFlag())),
            ExcelColumn.of("海事标识", vo -> flagText(vo.getMaritimeFlag())),
            ExcelColumn.of("承运人操作", ContainerVO::getCarrierOperate),
            ExcelColumn.of("码头箱况", ContainerVO::getCtrStatusTerminal),
            ExcelColumn.of("录入时间", ContainerVO::getInsertTime),
            ExcelColumn.of("更新时间", ContainerVO::getUpdateTime)
    );

    @Operation(summary = "分页查询集装箱(可按箱号前缀、状态精确筛选)")
    @GetMapping
    public Result<PageResult<ContainerVO>> page(ContainerQueryReq req) {
        return Result.success(containerService.page(req));
    }

    // ⚠️ /export 必须写在 /{no} **前面**。
    @Operation(summary = "导出集装箱为 Excel(筛选条件与列表一致, 不分页)")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(ContainerQueryReq req) {
        List<ContainerVO> rows = containerService.listForExport(req);
        byte[] body = excelExporter.toXlsx("集装箱", EXPORT_COLUMNS, rows);
        return ExcelResponse.of("containers", body);
    }

    @Operation(summary = "集装箱轨迹(按箱号查全部物流事件, 按发生时间升序, 可直接画时间轴)")
    @GetMapping("/{no}/track")
    public Result<List<ContainerEventVO>> track(@PathVariable String no) {
        return Result.success(containerEventService.track(no));
    }

    @Operation(summary = "集装箱概览(基础信息 + 最近一条物流事件)")
    @GetMapping("/{no}/summary")
    public Result<ContainerSummaryVO> summary(@PathVariable String no) {
        return Result.success(ContainerSummaryVO.of(
                containerService.getById(no),
                containerEventService.latestEvent(no)));
    }

    @Operation(summary = "集装箱下拉框(给新增装箱结果选箱子用: 箱号前缀搜索, 最多 20 条)")
    @GetMapping("/options")
    public Result<List<ContainerOptionVO>> options(
            @RequestParam(required = false) String keyword) {
        return Result.success(containerService.options(keyword));
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

    /** 标志位转中文: true->是, false->否, null->空 */
    private static String flagText(Boolean flag) {
        if (flag == null) {
            return null;
        }
        return flag ? "是" : "否";
    }
}
