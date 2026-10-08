package com.test.hangyun.excel;

import com.test.hangyun.constant.ExportConstants;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 把 Excel 字节数组包成 HTTP 响应。
 * <p>
 * ⚠️ 导出接口是全项目**唯一不返回 {@code Result}** 的地方 —— 它返回文件流。
 * 前端的处理方式也不一样: 普通接口读 JSON, 导出要按 blob 下载。
 * <p>
 * 抽成一个工具是为了让 20 多个导出接口的响应头**完全一致** —— 这类东西一旦各处手写,
 * 迟早出现有的地方写成老式的 {@code application/vnd.ms-excel}、有的地方漏了
 * {@code Content-Disposition}, 前端就得为每个接口写特例。
 */
public final class ExcelResponse {

    public static ResponseEntity<byte[]> of(String resourceName, byte[] body) {
        String fileName = ExportConstants.FILE_NAME_PATTERN.formatted(
                resourceName,
                LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(ExportConstants.XLSX_CONTENT_TYPE));
        // 用 ContentDisposition 而不是手拼字符串: 它负责处理引号转义等细节
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());
        // 内容长度已知就写上, 浏览器能显示下载进度
        headers.setContentLength(body.length);

        return ResponseEntity.ok().headers(headers).body(body);
    }

    /** 工具类, 不允许实例化 */
    private ExcelResponse() {
    }
}
