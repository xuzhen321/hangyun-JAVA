package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.ExportConstants;
import com.test.hangyun.constant.OptionConstants;
import com.test.hangyun.constant.PageConstants;
import com.test.hangyun.constant.UserConstants;
import com.test.hangyun.dto.UserCreateReq;
import com.test.hangyun.dto.UserPasswordReq;
import com.test.hangyun.dto.UserQueryReq;
import com.test.hangyun.dto.UserUpdateReq;
import com.test.hangyun.dto.vo.UserOptionVO;
import com.test.hangyun.dto.vo.UserVO;
import com.test.hangyun.mapper.UserMapper;
import com.test.hangyun.mapper.UserViewMapper;
import com.test.hangyun.log.OpLog;
import com.test.hangyun.pojo.entity.User;
import com.test.hangyun.pojo.enums.OpType;
import com.test.hangyun.pojo.view.UserView;
import com.test.hangyun.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 系统用户管理。
 * <p>
 * 查询走**视图 v_user**, 写走表 users。
 * <p>
 * 用户是**逻辑删除**(status = '2'), 行还在, 所以删除前不需要查引用
 * (设计文档 3.3: 逻辑删除的表不需要引用检查)。
 * <p>
 * ⚠️ **密码全程只进不出**: 明文只存在于请求 DTO 里, 落库前就哈希掉;
 * VO 上没有密码字段, 导出列里也没有。
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserViewMapper userViewMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<UserVO> page(UserQueryReq req) {
        long pageNo = PageConstants.normalizePage(req.getPage());
        long pageSize = PageConstants.normalizeSize(req.getSize());

        LambdaQueryWrapper<UserView> w = buildWrapper(req);
        // 最新录入的排最前(和其他业务实体一致)
        w.orderByDesc(UserView::getUserId);

        Page<UserView> p = userViewMapper.selectPage(new Page<>(pageNo, pageSize), w);
        return PageResult.of(p, UserVO::from);
    }

    @Override
    @OpLog(module = "系统用户", table = "users", type = OpType.EXPORT, desc = "导出 Excel")
    public List<UserVO> listForExport(UserQueryReq req) {
        LambdaQueryWrapper<UserView> w = buildWrapper(req);
        w.orderByDesc(UserView::getUserId);

        // 上限探测: 多取一行判断有没有超, 不要先 count(*) 再查一遍
        List<UserView> rows = userViewMapper.selectList(
                w.last("limit " + (ExportConstants.MAX_ROWS + 1)));
        if (rows.size() > ExportConstants.MAX_ROWS) {
            throw new BizException(
                    "导出行数超过 " + ExportConstants.MAX_ROWS + " 条，请缩小筛选范围后重试");
        }
        return rows.stream().map(UserVO::from).toList();
    }

    /** 列表和导出共用同一套筛选条件, 保证"列表里看到什么, 导出来就是什么" */
    private LambdaQueryWrapper<UserView> buildWrapper(UserQueryReq req) {
        LambdaQueryWrapper<UserView> w = new LambdaQueryWrapper<>();
        applyKeywordFilter(w, req.getKeyword());
        if (StringUtils.hasText(req.getStatus())) {
            // 显式指定状态时照实筛(传 2 就能看到已删除的)
            w.eq(UserView::getStatus, req.getStatus().trim());
        } else {
            // 逻辑删除: 不传状态时把已删除的藏起来。
            // 必须带上 is null 分支 —— 视图查询不受 @TableLogic 影响, 而且
            // SQL 里 "status <> '2'" 对 null 求值为 null(不成立), 会把没状态的行一起漏掉。
            w.and(q -> q.ne(UserView::getStatus, UserConstants.STATUS_DELETED)
                    .or().isNull(UserView::getStatus));
        }
        return w;
    }

    @Override
    public UserVO getById(Long id) {
        // 已删除的用户**详情仍然能查到**(和客户/港口一个口径), 所以这里不过滤状态
        UserView v = userViewMapper.selectById(id);
        if (v == null) {
            throw BizException.notFound("用户不存在");
        }
        return UserVO.from(v);
    }

    @Override
    public List<UserOptionVO> options(String keyword) {
        // 下拉框只要本表字段, 不需要联表, 所以读基表 users 而不是视图 v_user
        LambdaQueryWrapper<User> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            w.and(q -> q.likeRight(User::getUsername, kw).or().likeRight(User::getRealName, kw));
        }
        // 已删除的账号不能作为候选
        w.and(q -> q.ne(User::getStatus, UserConstants.STATUS_DELETED).or().isNull(User::getStatus));
        w.orderByAsc(User::getUsername);

        // searchCount=false: 下拉框不需要 total
        return userMapper.selectPage(new Page<>(1, OptionConstants.OPTION_LIMIT, false), w)
                .getRecords().stream().map(UserOptionVO::from).toList();
    }

    @Override
    @OpLog(module = "系统用户", table = "users", type = OpType.INSERT, desc = "新增用户")
    @Transactional
    public void create(UserCreateReq req) {
        String username = req.getUsername().trim();
        ensureUsernameUnique(username, null);

        User e = new User();
        e.setUsername(username);
        // 明文只在这里停留一行: 哈希之后就再也见不到原文了
        e.setPassword(passwordEncoder.encode(req.getPassword()));
        e.setRealName(trimToNull(req.getRealName()));
        e.setPhone(trimToNull(req.getPhone()));
        // 不传状态按"正常"处理
        e.setStatus(StringUtils.hasText(req.getStatus())
                ? req.getStatus().trim() : UserConstants.STATUS_NORMAL);
        // id 由数据库的自增序列生成(IdType.AUTO), 这里不赋值, 插入后 MyBatis-Plus 会回填
        // insertTime / updateTime 刻意不赋值, 留 null 交给触发器填北京时间
        userMapper.insert(e);
    }

    @Override
    @OpLog(module = "系统用户", table = "users", type = OpType.UPDATE, desc = "修改用户")
    @Transactional
    public void update(Long id, UserUpdateReq req) {
        User existing = getExisting(id);
        String username = req.getUsername().trim();
        // 查重时排除自己
        ensureUsernameUnique(username, id);

        // 不传状态 = 保持原状(见下面注释), 所以要先算出"改完之后是什么状态"
        String newStatus = StringUtils.hasText(req.getStatus())
                ? req.getStatus().trim() : existing.getStatus();
        guardBuiltinAdmin(existing, newStatus);

        // 整体覆盖: 每个字段都显式 set。
        // ⚠️ status 是唯一的例外 —— 不传就保持原状, 而不是写 null。
        //    理由: 状态写 null 的账号既登不进去、也不会出现在任何列表里(列表用 status <> '2' 过滤),
        //    等于被"静默停用", 前端漏传一个字段就出事, 代价太大。
        LambdaUpdateWrapper<User> u = new LambdaUpdateWrapper<>();
        u.eq(User::getId, id)
                .set(User::getUsername, username)
                .set(User::getRealName, trimToNull(req.getRealName()))
                .set(User::getPhone, trimToNull(req.getPhone()))
                .set(User::getStatus, newStatus);
        userMapper.update(null, u);
    }

    @Override
    @OpLog(module = "系统用户", table = "users", type = OpType.UPDATE, desc = "重置用户密码")
    @Transactional
    public void resetPassword(Long id, UserPasswordReq req) {
        getExisting(id);

        LambdaUpdateWrapper<User> u = new LambdaUpdateWrapper<>();
        u.eq(User::getId, id)
                .set(User::getPassword, passwordEncoder.encode(req.getNewPassword()));
        userMapper.update(null, u);
    }

    @Override
    @OpLog(module = "系统用户", table = "users", type = OpType.DELETE, desc = "删除用户")
    @Transactional
    public void delete(Long id) {
        User existing = getExisting(id);
        if (UserConstants.isBuiltinAdmin(existing.getId())) {
            throw BizException.conflict("内置管理员账号不允许删除");
        }
        // 逻辑删除: 不删行, 只把 status 改成 '2'(已删除)。
        // 已经是已删除时重复调用也安全: 整行没有实际变化, 触发器不会刷新 update_time。
        LambdaUpdateWrapper<User> u = new LambdaUpdateWrapper<>();
        u.eq(User::getId, id).set(User::getStatus, UserConstants.STATUS_DELETED);
        userMapper.update(null, u);
    }

    @Override
    @OpLog(module = "系统用户", table = "users", type = OpType.DELETE, desc = "批量删除用户")
    @Transactional
    public void deleteBatch(List<Long> ids) {
        List<Long> distinctIds = ids.stream().distinct().toList();
        if (distinctIds.isEmpty()) {
            return;
        }
        // 严格语义: 批次里有内置管理员就整批拒绝, 不做部分删除 ——
        // 悄悄少删一个的话, 用户会以为全删成功了。
        if (distinctIds.contains(UserConstants.BUILTIN_ADMIN_ID)) {
            throw BizException.conflict("内置管理员账号不允许删除");
        }
        // 逻辑删除, 一条 UPDATE 覆盖整批; 不存在的 id 匹配不到, 自然被忽略(幂等)
        LambdaUpdateWrapper<User> u = new LambdaUpdateWrapper<>();
        u.in(User::getId, distinctIds).set(User::getStatus, UserConstants.STATUS_DELETED);
        userMapper.update(null, u);
    }

    /** 写操作的存在性检查走**基表**: 已删除的行也要能改/能重复删, 所以不读视图、不过滤状态 */
    private User getExisting(Long id) {
        User e = userMapper.selectById(id);
        if (e == null) {
            throw BizException.notFound("用户不存在");
        }
        return e;
    }

    /**
     * username 在库里有唯一约束, 提前查重以便返回 409 而不是撞唯一键报 500。
     * <p>
     * ⚠️ **故意连已删除的用户一起数**: 逻辑删除只改 status, 行还在表里, 唯一索引照样拦它。
     * 这里要是把 status='2' 的排除掉, 新增就会先通过查重、再撞唯一键变成 500。
     * <p>
     * 也就是说——**删掉的用户名不能被重新注册**。这是逻辑删除 + 唯一约束组合的必然结果,
     * 要支持"删了还能重名"就得改成物理删除或者调整唯一键。
     */
    private void ensureUsernameUnique(String username, Long excludeId) {
        LambdaQueryWrapper<User> w = new LambdaQueryWrapper<>();
        w.eq(User::getUsername, username);
        w.ne(excludeId != null, User::getId, excludeId);
        if (userMapper.selectCount(w) > 0) {
            throw BizException.conflict("登录名已存在: " + username);
        }
    }

    /**
     * 内置管理员(id=1)**不能被冻结或删除**。
     * <p>
     * 否则一旦把唯一的管理员置成"冻结/已删除", 就没人能登录进来改回去了 ——
     * 只能去数据库里手工 UPDATE。改密码、改姓名电话不受限制。
     */
    private void guardBuiltinAdmin(User existing, String newStatus) {
        if (!UserConstants.isBuiltinAdmin(existing.getId())) {
            return;
        }
        if (newStatus != null && !UserConstants.STATUS_NORMAL.equals(newStatus)) {
            throw BizException.conflict("内置管理员账号不能被冻结或删除");
        }
    }

    /** 视图上的关键字过滤: 登录名 / 真实姓名 前缀, 任一命中 */
    private void applyKeywordFilter(LambdaQueryWrapper<UserView> w, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        String kw = keyword.trim();
        w.and(q -> q.likeRight(UserView::getUsername, kw)
                .or().likeRight(UserView::getRealName, kw));
    }

    /** 空字符串和全空格都归一成 null, 免得库里混进 "" 和 null 两种"没填" */
    private static String trimToNull(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        return s.trim();
    }
}
