package com.test.hangyun.controller;

import com.test.hangyun.common.PageResult;
import com.test.hangyun.common.Result;
import com.test.hangyun.dto.UserBatchDeleteReq;
import com.test.hangyun.dto.UserCreateReq;
import com.test.hangyun.dto.UserPasswordReq;
import com.test.hangyun.dto.UserQueryReq;
import com.test.hangyun.dto.UserUpdateReq;
import com.test.hangyun.dto.vo.UserOptionVO;
import com.test.hangyun.dto.vo.UserVO;
import com.test.hangyun.excel.ExcelColumn;
import com.test.hangyun.excel.ExcelExporter;
import com.test.hangyun.excel.ExcelResponse;
import com.test.hangyun.service.UserService;
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
 * 系统用户。
 * <p>
 * 删除是**逻辑删除**(status 置 '2')。内置管理员(id=1)不允许删除/冻结。
 * <p>
 * ⚠️ 所有响应都**不含密码**。改密码走单独的 {@code PUT /users/{id}/password}。
 */
@Tag(name = "系统用户", description = "用户账号的增删改查 + 重置密码")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final ExcelExporter excelExporter;

    /**
     * 导出列。**表头用中文**, {@code status} 导中文状态而不是 '0'/'1'/'2';
     * **没有密码列**(和列表/详情一个口径)。
     */
    private static final List<ExcelColumn<UserVO>> EXPORT_COLUMNS = List.of(
            ExcelColumn.of("用户ID", UserVO::getId),
            ExcelColumn.of("登录名", UserVO::getUsername),
            ExcelColumn.of("真实姓名", UserVO::getRealName),
            ExcelColumn.of("电话", UserVO::getPhone),
            ExcelColumn.of("状态", vo -> statusText(vo.getStatus())),
            ExcelColumn.of("录入时间", UserVO::getInsertTime),
            ExcelColumn.of("更新时间", UserVO::getUpdateTime)
    );

    @Operation(summary = "分页查询用户(可按登录名/真实姓名前缀搜索, 按状态筛选)")
    @GetMapping
    public Result<PageResult<UserVO>> page(UserQueryReq req) {
        return Result.success(userService.page(req));
    }

    // ⚠️ /export 必须写在 /{id} **前面**。
    @Operation(summary = "导出用户为 Excel(筛选条件与列表一致, 不分页)")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(UserQueryReq req) {
        List<UserVO> rows = userService.listForExport(req);
        byte[] body = excelExporter.toXlsx("用户", EXPORT_COLUMNS, rows);
        return ExcelResponse.of("users", body);
    }

    @Operation(summary = "用户下拉框(登录名/真实姓名前缀, 最多 20 条)")
    @GetMapping("/options")
    public Result<List<UserOptionVO>> options(
            @RequestParam(required = false) String keyword) {
        return Result.success(userService.options(keyword));
    }

    @Operation(summary = "查询用户详情")
    @GetMapping("/{id}")
    public Result<UserVO> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody UserCreateReq req) {
        userService.create(req);
        return Result.success();
    }

    @Operation(summary = "修改用户(不含密码)")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdateReq req) {
        userService.update(id, req);
        return Result.success();
    }

    @Operation(summary = "重置密码")
    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id,
                                      @Valid @RequestBody UserPasswordReq req) {
        userService.resetPassword(id, req);
        return Result.success();
    }

    @Operation(summary = "批量删除用户(逻辑删除; 批次含内置管理员则整批拒绝)")
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@Valid @RequestBody UserBatchDeleteReq req) {
        userService.deleteBatch(req.getIds());
        return Result.success();
    }

    @Operation(summary = "删除用户(逻辑删除; 内置管理员不允许)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    /** 账号状态转中文: '0'正常 / '1'冻结 / '2'已删除 */
    private static String statusText(String status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case "0" -> "正常";
            case "1" -> "冻结";
            case "2" -> "已删除";
            default -> status;
        };
    }
}
