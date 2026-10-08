package com.test.hangyun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.test.hangyun.auth.CurrentUser;
import com.test.hangyun.auth.JwtTokenProvider;
import com.test.hangyun.auth.UserContext;
import com.test.hangyun.common.exception.BizException;
import com.test.hangyun.constant.AuthConstants;
import com.test.hangyun.constant.UserConstants;
import com.test.hangyun.dto.LoginReq;
import com.test.hangyun.dto.vo.LoginVO;
import com.test.hangyun.dto.vo.UserVO;
import com.test.hangyun.log.OperationLogWriter;
import com.test.hangyun.mapper.UserMapper;
import com.test.hangyun.mapper.UserViewMapper;
import com.test.hangyun.pojo.entity.User;
import com.test.hangyun.pojo.enums.OpType;
import com.test.hangyun.pojo.view.UserView;
import com.test.hangyun.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 登录认证。
 * <p>
 * 令牌是**无状态 JWT**: 服务端不存会话, 只靠签名 + 过期时间判断有效性。
 * 好处是无状态、好扩展; 代价是**登出无法在服务端强制生效**(见 {@link #logout}),
 * 而且冻结一个账号不会立刻踢掉它手上还没过期的令牌(见 {@link #currentUser} 的处理)。
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final UserViewMapper userViewMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final OperationLogWriter operationLogWriter;

    @Override
    public LoginVO login(LoginReq req) {
        User user = findByUsername(req.getUsername().trim());

        // ① 账号不存在 和 ② 密码不对 —— **故意返回同一句话**。
        //    分开说等于提供了一个用户名枚举接口: 试出"用户名存在"之后再慢慢撞密码。
        //
        //    这里也顺带覆盖了"库里 password 为 null"的脏数据: BCryptPasswordEncoder.matches
        //    遇到空的 encoded 会记一条日志并返回 false, 不会抛异常。
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BizException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }

        // ③ 账号状态必须是"正常"。冻结和已删除一律拒绝。
        //    这里**和密码错误分开报**, 因为它不是"猜错了", 告诉用户原因才有助于找回账号。
        if (!UserConstants.canLogin(user.getStatus())) {
            throw new BizException(HttpStatus.FORBIDDEN, "账号已被冻结或停用，请联系管理员");
        }

        LoginVO vo = new LoginVO();
        vo.setToken(tokenProvider.issue(user.getId(), user.getUsername()));
        vo.setExpireMinutes(tokenProvider.getExpireMillis() / 60_000L);
        vo.setUser(UserVO.from(user));

        // 登录要留痕(审计的需要: 谁在什么时候进过系统)。
        // ⚠️ **这里没有走 @OpLog 切面**, 是手工写的 —— 因为登录成功的那一刻
        //    UserContext 还是空的(登录接口在白名单里, 拦截器没给它塞用户),
        //    切面取不到 user_id, 只能由这里把刚认证出来的用户 id 显式传进去。
        //
        // ⚠️ 有意的取舍: **只记成功登录, 不记失败**。失败时不知道是谁(账号可能根本不存在),
        //    硬记会把"试过的用户名"写进 target_id, 语义上不对。真要审计爆破行为,
        //    应该给失败单独加一个类型, 那是另一件事。
        operationLogWriter.writeSafely(
                user.getId(), OpType.LOGIN, "users", String.valueOf(user.getId()),
                null, true, null);

        return vo;
    }

    @Override
    public UserVO currentUser() {
        CurrentUser current = UserContext.get();
        if (current == null) {
            // 正常走不到: /auth/me 不在白名单里, 拦截器已经保证登录了。纯兜底。
            throw new BizException(HttpStatus.UNAUTHORIZED, AuthConstants.MSG_UNAUTHORIZED);
        }

        UserView v = userViewMapper.selectById(current.id());
        if (v == null || !UserConstants.canLogin(v.getStatus())) {
            // 令牌本身还有效, 但账号已经被删掉/冻结了。
            // **每次都回库查一次状态**, 正是为了让"冻结/删除"能立即生效 ——
            // 如果只信令牌里的内容, 被冻结的人能一直用到令牌过期为止。
            throw new BizException(HttpStatus.UNAUTHORIZED, AuthConstants.MSG_UNAUTHORIZED);
        }
        return UserVO.from(v);
    }

    @Override
    public void logout() {
        // JWT 是无状态的, 服务端没有会话可以销毁 —— 登出的实质是**前端把令牌丢掉**。
        // 所以这里刻意什么都不做, 但仍然提供这个接口:
        //   1. 前端只需要"调一下, 然后清本地存储", 不用自己判断要不要调;
        //   2. 以后若要改成"服务端强制失效"(令牌黑名单 / 用户级 token 版本号),
        //      改这里即可, 前端无感。
        //
        // ⚠️ 由此带来一个已知行为: 登出后那个令牌在过期前**技术上仍然可用**。
        //    要堵这个口子必须引入黑名单, 属于另一个话题。
    }

    private User findByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        LambdaQueryWrapper<User> w = new LambdaQueryWrapper<>();
        w.eq(User::getUsername, username);
        // username 有唯一约束, 最多一条; 用 selectOne 之外的写法是为了容忍脏数据不抛异常
        return userMapper.selectList(w).stream().findFirst().orElse(null);
    }
}
