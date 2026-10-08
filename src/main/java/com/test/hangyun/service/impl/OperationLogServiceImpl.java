package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.ExportConstants;
import com.test.hangyun.constant.PageConstants;
import com.test.hangyun.dto.OperationLogQueryReq;
import com.test.hangyun.dto.vo.OperationLogVO;
import com.test.hangyun.log.OpLog;
import com.test.hangyun.mapper.OperationLogViewMapper;
import com.test.hangyun.pojo.enums.OpType;
import com.test.hangyun.pojo.view.OperationLogView;
import com.test.hangyun.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 操作日志查询。
 * <p>
 * 全部走**视图 v_operation_log** —— 操作人姓名和操作类型名称视图里都联好了,
 * 不用 Service 二次组装(和物流事件模块一个路子)。
 * <p>
 * 只读: 没有 create / update / delete。日志由 {@code @OpLog} + 切面自动写入。
 */
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogViewMapper logViewMapper;

    @Override
    public PageResult<OperationLogVO> page(OperationLogQueryReq req) {
        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        LambdaQueryWrapper<OperationLogView> w = buildWrapper(req);
        // 最近发生的排最前
        w.orderByDesc(OperationLogView::getOperationTime);

        Page<OperationLogView> p = logViewMapper.selectPage(new Page<>(pageNo, pageSize), w);
        return PageResult.of(p, OperationLogVO::from);
    }

    @Override
    public OperationLogVO getById(Long id) {
        OperationLogView v = logViewMapper.selectById(id);
        if (v == null) {
            throw BizException.notFound("操作日志不存在");
        }
        return OperationLogVO.from(v);
    }

    @Override
    @OpLog(module = "操作日志", table = "log", type = OpType.EXPORT, desc = "导出 Excel")
    public List<OperationLogVO> listForExport(OperationLogQueryReq req) {
        LambdaQueryWrapper<OperationLogView> w = buildWrapper(req);
        w.orderByDesc(OperationLogView::getOperationTime);

        // ⚠️ 上限检查用"多取一行"而不是先 count(*):
        //    多取一行只是把返回集放大 1 条, 而 count(*) 要在同一批条件下让数据库
        //    再扫一遍 —— 为了知道"超没超"多跑一遍全表, 不划算。
        int probe = ExportConstants.MAX_ROWS + 1;
        List<OperationLogView> rows = logViewMapper.selectList(w.last("limit " + probe));
        if (rows.size() > ExportConstants.MAX_ROWS) {
            throw new BizException(
                    "导出行数超过 " + ExportConstants.MAX_ROWS + " 条，请缩小筛选范围后重试");
        }

        return rows.stream().map(OperationLogVO::from).toList();
    }

    /**
     * 列表和导出**共用**同一套筛选条件, 保证"列表里看到什么, 导出来就是什么"。
     * <p>
     * ⚠️ 每个带条件的 eq 都先把值算好再传: MyBatis-Plus 的
     * {@code eq(condition, column, value)} 里, Java 参数是**急切求值**的 ——
     * 直接在参数位置写 {@code req.getX().trim()} 的话, 不传该参数时那个 trim 会对 null
     * 动手, 直接 NPE 500。项目里已经踩过一次(见 ContainerEventServiceImpl 的注释)。
     */
    private LambdaQueryWrapper<OperationLogView> buildWrapper(OperationLogQueryReq req) {
        LambdaQueryWrapper<OperationLogView> w = new LambdaQueryWrapper<>();

        String targetTable = StringUtils.hasText(req.getTargetTable())
                ? req.getTargetTable().trim() : null;
        String resultStatus = StringUtils.hasText(req.getResultStatus())
                ? req.getResultStatus().trim() : null;

        w.eq(req.getUserId() != null, OperationLogView::getUserId, req.getUserId());
        w.eq(req.getOperationTypeId() != null,
                OperationLogView::getOperationTypeId, req.getOperationTypeId());
        w.eq(targetTable != null, OperationLogView::getTargetTable, targetTable);
        w.eq(resultStatus != null, OperationLogView::getResultStatus, resultStatus);
        // 操作时间闭区间, 单边不传就只限一边
        w.ge(req.getOperationTimeFrom() != null,
                OperationLogView::getOperationTime, req.getOperationTimeFrom());
        w.le(req.getOperationTimeTo() != null,
                OperationLogView::getOperationTime, req.getOperationTimeTo());

        // 关键字是"包含匹配"(不是前缀), 用在"找某个人干过什么"这种场景更顺手
        if (StringUtils.hasText(req.getKeyword())) {
            String kw = req.getKeyword().trim();
            w.and(q -> q.like(OperationLogView::getOperatorName, kw)
                    .or().like(OperationLogView::getTargetId, kw));
        }

        return w;
    }
}
