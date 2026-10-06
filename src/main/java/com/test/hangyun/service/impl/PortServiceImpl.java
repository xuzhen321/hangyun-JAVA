package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.constant.OptionConstants;
import com.test.hangyun.constant.PortConstants;
import com.test.hangyun.dto.vo.PortOptionVO;
import com.test.hangyun.mapper.PortMapper;
import com.test.hangyun.pojo.entity.Port;
import com.test.hangyun.service.PortService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 港口。
 * <p>
 * 目前只读基表 port, 不走视图 v_port —— 下拉框只要本表的中英文名和五字码,
 * 而 v_port 联了 7 张表, 每次按键都跑一遍太重。将来做港口列表/详情(要带国家、区域、时区名)
 * 时再走视图。
 */
@Service
@RequiredArgsConstructor
public class PortServiceImpl implements PortService {

    private final PortMapper portMapper;

    @Override
    public List<PortOptionVO> options(String keyword) {
        LambdaQueryWrapper<Port> w = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            // 中文名 / 英文名 / 五字码, 任一前缀命中即可; 用 and(...) 把三个 OR 包起来,
            // 否则后面的状态过滤会被 OR 拆散, 变成"删掉的港口也可能被查出来"
            w.and(q -> q.likeRight(Port::getCnname, kw)
                    .or().likeRight(Port::getEnname, kw)
                    .or().likeRight(Port::getUnlocode, kw));
        }

        // 港口是逻辑删除: state = '3' 表示已删除, 查询必须手工排除(文档 3.6 提醒过)。
        // state 允许为 null, 而 "state <> '3'" 对 null 求值为 null(视为不成立),
        // 会连"没有状态"的港口一起漏掉, 所以必须额外放行 null。
        w.and(q -> q.ne(Port::getState, PortConstants.STATE_DELETED)
                .or().isNull(Port::getState));

        w.orderByAsc(Port::getUnlocode);

        // 第三个参数 searchCount=false: 下拉框不需要 total, 省掉那条 COUNT
        return portMapper.selectPage(
                        new Page<>(1, OptionConstants.OPTION_LIMIT, false), w)
                .getRecords().stream().map(PortOptionVO::from).toList();
    }
}
