package com.test.hangyun.excel;

import com.test.hangyun.constant.DateTimeConstants;
import com.test.hangyun.constant.ExportConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 生成 Excel(.xlsx)。
 * <p>
 * 用的是 POI 的 {@link SXSSFWorkbook} —— 它是 SXSSF(**流式**)的那一版:
 * 数据一行行写, 只有最近 {@link ExportConstants#STREAM_WINDOW_ROWS} 行留在内存里,
 * 更早的行已经刷进磁盘临时文件了。所以内存占用和总行数无关。
 * (对照: {@code XSSFWorkbook} 会把整个工作簿留在内存, 几万行就能 OOM。)
 * <p>
 * ⚠️ **先写进内存的 {@link ByteArrayOutputStream}, 再整个交给调用方** —— 不是直接往
 * HTTP 响应流里写。代价是峰值内存多一份文件大小, 换来的是**出错时能干净地返回错误**:
 * 一旦开始往响应流里写, 状态码和响应头就发出去了, 后面再出错只能给用户一个
 * **打不开的损坏 xlsx**, 而不是一句"导出失败"。
 */
@Slf4j
@Component
public class ExcelExporter {

    /**
     * 把数据写成 xlsx 字节数组。
     *
     * @param sheetName 工作表名(会显示在 Excel 底部标签上)
     * @param columns   列定义, 顺序即列顺序
     * @param rows      数据行, 可以为空(那样只导出表头)
     */
    public <T> byte[] toXlsx(String sheetName, List<ExcelColumn<T>> columns, List<T> rows) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(ExportConstants.STREAM_WINDOW_ROWS);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(sheetName);
            CellStyle headerStyle = buildHeaderStyle(workbook);

            // 表头
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns.get(i).header());
                cell.setCellStyle(headerStyle);
                // 固定列宽: 自动列宽(trackAllColumnsForAutoSizing)在流式模式下会把窗口里的行
                // 留在内存, 正好抵消掉 SXSSF 的意义。固定宽度够用了。
                sheet.setColumnWidth(i, 18 * 256);
            }

            // 数据行(从第 1 行开始, 0 被表头占了)
            int rowIndex = 1;
            for (T item : rows) {
                Row row = sheet.createRow(rowIndex++);
                for (int c = 0; c < columns.size(); c++) {
                    writeCell(row.createCell(c), columns.get(c).value().apply(item));
                }
            }

            workbook.write(out);
            // dispose 会删掉 SXSSF 落盘的临时文件。close() 在新版里也会做, 这里显式写一遍
            // 是为了把意图说清楚 —— 不 dispose 的话临时文件会一直堆在系统临时目录里。
            workbook.dispose();
            return out.toByteArray();
        } catch (Exception e) {
            // 包装成运行时异常往上抛: 交给全局异常处理器变成 500 的 JSON。
            // 此时**还没有往响应流写任何东西**, 所以前端能正常识别这是一次失败。
            throw new IllegalStateException("生成 Excel 失败", e);
        }
    }

    /** 按值的实际类型写单元格。POI 支持的类型有限, 其余一律走文本 */
    private void writeCell(Cell cell, Object value) {
        if (value == null) {
            return;
        }
        switch (value) {
            case String s -> cell.setCellValue(s);
            case Number n -> cell.setCellValue(n.doubleValue());
            case Boolean b -> cell.setCellValue(b);
            // 时间统一用和 JSON 响应一样的格式(yyyy-MM-dd HH:mm:ss), 不带 T 和毫秒 ——
            // 免得同一个系统里两种时间写法并存
            case LocalDateTime t -> cell.setCellValue(t.format(DateTimeConstants.OUT_FORMATTER));
            case java.time.LocalDate d -> cell.setCellValue(d.toString());
            default -> cell.setCellValue(String.valueOf(value));
        }
    }

    private CellStyle buildHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }
}
