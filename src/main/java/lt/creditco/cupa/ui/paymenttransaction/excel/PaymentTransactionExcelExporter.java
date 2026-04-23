package lt.creditco.cupa.ui.paymenttransaction.excel;

import com.bpmid.excel_exporter.ExcelExporter;
import com.bpmid.excel_exporter.ExportOptions;
import java.io.IOException;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import lt.creditco.cupa.service.dto.PaymentTransactionDTO;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Builds XLSX for the payment transaction list using the same rows as the grid (order preserved).
 */
@Service
public class PaymentTransactionExcelExporter {

    private static final Logger log = LoggerFactory.getLogger(PaymentTransactionExcelExporter.class);

    public byte[] exportToExcel(List<PaymentTransactionDTO> visibleRows, ZoneId timeZone) {
        log.info("Exporting {} payment transactions to Excel", visibleRows == null ? 0 : visibleRows.size());

        List<PaymentTransactionDTO> rows = visibleRows == null ? List.of() : visibleRows;
        List<PaymentTransactionExportData> exportData = rows
            .stream()
            .map(PaymentTransactionExportData::new)
            .collect(Collectors.toList());

        Workbook workbook = createWorkbook(exportData, timeZone);
        try {
            return ExcelExporter.toByteArray(workbook);
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                log.error("Failed to close workbook", e);
            }
        }
    }

    private Workbook createWorkbook(List<PaymentTransactionExportData> exportData, ZoneId timeZone) {
        Workbook workbook = ExcelExporter.createWorkbook();
        ZoneId zone = timeZone != null ? timeZone : ZoneId.systemDefault();
        try {
            ExportOptions options = ExportOptions.builder()
                .autoFilter(true)
                .freezeHeader(true)
                .freezedColumns(2)
                .autoSizeColumns(true)
                .timeZone(zone)
                .build();
            ExcelExporter.exportToSheet(
                workbook,
                "payment_transactions",
                exportData,
                PaymentTransactionExportData.class,
                options
            );
        } catch (IllegalAccessException e) {
            log.error("Failed to generate Excel due to field access error", e);
            throw new RuntimeException("Failed to generate Excel due to field access error", e);
        }
        return workbook;
    }
}
