package com.phenikaa.library;

import com.phenikaa.library.model.Document;
import com.phenikaa.library.model.Document.DocumentType;
import com.phenikaa.library.model.PhysicalCopy;
import com.phenikaa.library.service.AccessionService;
import com.phenikaa.library.service.CatalogingService;
import com.phenikaa.library.service.LabelPrinter;

import java.util.ArrayList;
import java.util.List;

/**
 * MAIN APPLICATION – Hệ thống Biên mục Tài liệu Thư viện Phenikaa
 *
 * Demo toàn bộ 5 bước theo quy trình QT.02.TV.NV:
 *
 *   Bước 1: Tiếp nhận tài liệu mới          (nhập dữ liệu, tạo phiếu nhập kho)
 *   Bước 2: Phân kho, đóng dấu, dán mã vạch (sinh ĐKCB + barcode)
 *   Bước 3: Xử lý nghiệp vụ                 (DDC, Cutter, Call Number, Koha)
 *   Bước 4: In nhãn và dán gáy              (spine label + barcode label)
 *   Bước 5: Giao về các kho để xếp giá      (chuyển trạng thái AVAILABLE)
 */
public class MainApp {

    public static void main(String[] args) {

        // Khởi tạo các service
        AccessionService  accessionService  = new AccessionService();
        CatalogingService catalogingService = new CatalogingService();
        LabelPrinter      labelPrinter      = new LabelPrinter();

        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║    HỆ THỐNG BIÊN MỤC TÀI LIỆU – THƯ VIỆN PHENIKAA      ║");
        System.out.println("║    Quy trình: QT.02.TV.NV | Mã số: QT.02.TV.NV          ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        List<Document> processedDocs = new ArrayList<>();

        // ── Demo xử lý 3 tài liệu khác nhau ──────────────────────────────────
        processedDocs.add(runFullCatalogingWorkflow(
                accessionService, catalogingService, labelPrinter,
                buildDoc1(), 3, "Nguyễn Thị Hương"
        ));

        processedDocs.add(runFullCatalogingWorkflow(
                accessionService, catalogingService, labelPrinter,
                buildDoc2(), 1, "Nguyễn Thị Hương"
        ));

        processedDocs.add(runFullCatalogingWorkflow(
                accessionService, catalogingService, labelPrinter,
                buildDoc3(), 2, "Nguyễn Thị Hương"
        ));

        // ── Thông báo sách mới (BM.04.QT.02.TV.NV) ───────────────────────────
        System.out.println("\n\n════════════════════════════════════════════════════════════");
        labelPrinter.printNewBookNotice(processedDocs, "4", 2024);

        // ── Tóm tắt kết quả ──────────────────────────────────────────────────
        printSummary(processedDocs);
    }

    // ─── Quy trình 5 bước đầy đủ ─────────────────────────────────────────────

    /**
     * Chạy toàn bộ 5 bước quy trình biên mục cho một tài liệu.
     *
     * @param doc        Tài liệu cần biên mục
     * @param copyCount  Số bản cần xử lý
     * @param staffName  Tên nhân viên nghiệp vụ
     */
    private static Document runFullCatalogingWorkflow(
            AccessionService accessionService,
            CatalogingService catalogingService,
            LabelPrinter labelPrinter,
            Document doc,
            int copyCount,
            String staffName) {

        System.out.println("\n\n════════════════════════════════════════════════════════════");
        System.out.printf("▶ XỬ LÝ: \"%s\"%n", doc.getTitle());
        System.out.println("════════════════════════════════════════════════════════════");

        // ── BƯỚC 1: Tiếp nhận tài liệu mới ───────────────────────────────────
        System.out.println("\n── BƯỚC 1: TIẾP NHẬN TÀI LIỆU MỚI ─────────────────────────");

        // Validate ISBN
        if (doc.getIsbn() != null && !doc.getIsbn().isBlank()) {
            boolean isbnOk = accessionService.validateIsbn(doc.getIsbn());
            System.out.printf("  ISBN: %s → %s%n",
                    doc.getIsbn(), isbnOk ? "✓ Hợp lệ" : "✗ Không hợp lệ");
        }

        // Tạo phiếu nhập kho (BM.01.QT.02.TV.NV)
        String sourceDoc = String.format("BM.01.%s.%d",
                doc.getDocumentType() != null ? doc.getDocumentType().getCode() : "TL",
                System.currentTimeMillis() % 10000);
        doc.setSourceDocument(sourceDoc);

        System.out.printf("  Phiếu nhập kho: %s%n", sourceDoc);
        System.out.printf("  Số bản nhận: %d | Loại: %s%n",
                copyCount,
                doc.getDocumentType() != null ? doc.getDocumentType().getLabel() : "Chưa xác định");
        System.out.println("  ✓ Tiếp nhận – Đối chiếu danh mục HOÀN THÀNH");

        // Tạo các bản sao vật lý
        for (int i = 1; i <= copyCount; i++) {
            doc.addCopy(new PhysicalCopy(doc, i));
        }

        // ── BƯỚC 2: Phân kho, đóng dấu, dán mã vạch ──────────────────────────
        for (PhysicalCopy copy : doc.getCopies()) {
            accessionService.processStep2(copy, staffName);
        }

        // ── BƯỚC 3: Xử lý nghiệp vụ (DDC, Cutter, Call Number) ───────────────
        catalogingService.performCataloging(doc, staffName);

        // ── BƯỚC 4: In nhãn và dán gáy ────────────────────────────────────────
        labelPrinter.processStep4(doc, staffName);

        // In nhãn mã vạch đầy đủ cho bản đầu tiên (minh hoạ)
        System.out.println("\n  [Nhãn mã vạch bản 1]");
        labelPrinter.printBarcodeLabel(doc.getCopies().get(0));

        // ── BƯỚC 5: Giao về kho để xếp giá ───────────────────────────────────
        System.out.println("\n── BƯỚC 5: GIAO VỀ KHO ĐỂ XẾP GIÁ ─────────────────────────");
        for (PhysicalCopy copy : doc.getCopies()) {
            // Xác định vị trí giá theo Call Number và kho
            String shelf = assignShelfLocation(doc, copy.getCopyNumber());
            copy.setShelfLocation(shelf);
            copy.setRoomCode(doc.getWarehouseLocation().getCode());
            copy.setDeliveredDate(java.time.LocalDate.now());
            copy.setStatus(PhysicalCopy.CopyStatus.AVAILABLE);

            System.out.printf("  Bản %d → Kho: %s | Giá: %s | ĐKCB: %s%n",
                    copy.getCopyNumber(),
                    doc.getWarehouseLocation().getLabel(),
                    shelf,
                    copy.getAccessionNumber());
        }

        // Ghi nhận biên bản bàn giao (BM.03.QT.02.TV.NV)
        System.out.printf("  Biên bản bàn giao: BM.03.QT.02.TV.NV – %d bản%n",
                doc.getCopies().size());
        System.out.println("  ✓ Giao về kho HOÀN THÀNH");

        // Kiểm tra hoàn thành toàn bộ quy trình
        boolean allDone = doc.getCopies().stream().allMatch(PhysicalCopy::isFullyProcessed);
        System.out.printf("%n  ══ KẾT QUẢ: %s ══%n",
                allDone ? "✅ HOÀN THÀNH TOÀN BỘ QUY TRÌNH" : "⚠ CÒN BƯỚC CHƯA HOÀN THÀNH");

        return doc;
    }

    // ─── Xác định vị trí giá sách ────────────────────────────────────────────

    /**
     * Xác định vị trí giá sách dựa trên DDC và kho.
     *
     * Quy tắc xếp giá tại Phenikaa:
     *   - Phòng đọc mở:  xếp theo môn loại DDC (A1 = 000-099, A2 = 100-199, ...)
     *   - Phòng bảo quản: xếp theo số ĐKCB
     *   - Kho giáo trình: xếp theo môn giảng dạy
     */
    private static String assignShelfLocation(Document doc, int copyNum) {
        if (doc.getDdcCode() == null) return "TBD";

        int ddcMain = 0;
        try {
            ddcMain = Integer.parseInt(doc.getDdcCode().replaceAll("\\.", "").substring(0, 3));
        } catch (Exception e) {
            return "TBD";
        }

        char row = (char) ('A' + (ddcMain / 100));     // A=000s, B=100s, ...
        int  bay  = (ddcMain % 100) / 10 + 1;          // kệ trong dãy
        int  shelf = (ddcMain % 10) + 1;               // tầng trong kệ

        return String.format("%c%d-%d", row, bay, shelf + (copyNum - 1) / 5);
    }

    // ─── Dữ liệu mẫu ─────────────────────────────────────────────────────────

    private static Document buildDoc1() {
        Document doc = new Document(
                "9786047574162",
                "Lập trình Java nâng cao",
                "Nguyễn Văn An",
                "NXB Bách Khoa Hà Nội",
                2023
        );
        doc.setDocumentType(DocumentType.GIAO_TRINH);
        doc.setSubjectHeading("Lập trình Java; Spring Framework; JPA");
        doc.setKeywords("java; spring; hibernate; lập trình");
        doc.setLanguage("vi");
        doc.setTotalPages(456);
        doc.setEdition(2);
        doc.setUnitPrice(185000);
        return doc;
    }

    private static Document buildDoc2() {
        Document doc = new Document(
                "9786048001583",
                "Phân tích và thiết kế hướng đối tượng với UML",
                "Trần Thị Bích",
                "NXB Khoa học Kỹ thuật",
                2022
        );
        doc.setDocumentType(DocumentType.TAI_LIEU_THAM_KHAO);
        doc.setSubjectHeading("Phân tích thiết kế; UML; Hướng đối tượng");
        doc.setKeywords("uml; oop; object oriented; design pattern");
        doc.setLanguage("vi");
        doc.setTotalPages(312);
        doc.setUnitPrice(145000);
        return doc;
    }

    private static Document buildDoc3() {
        Document doc = new Document(
                null,   // Luận văn không có ISBN
                "Nghiên cứu ứng dụng Machine Learning trong dự báo tín dụng",
                "Lê Minh Châu",
                "Trường Đại học Phenikaa",
                2024
        );
        doc.setDocumentType(DocumentType.LUAN_VAN);
        doc.setSubjectHeading("Machine learning; Tín dụng; Mô hình dự báo");
        doc.setKeywords("machine learning; ai; credit scoring; random forest");
        doc.setLanguage("vi");
        doc.setTotalPages(98);
        doc.setUnitPrice(0);
        return doc;
    }

    // ─── Tóm tắt ─────────────────────────────────────────────────────────────

    private static void printSummary(List<Document> docs) {
        System.out.println("\n\n════════════════════════════════════════════════════════════");
        System.out.println("  TÓM TẮT KẾT QUẢ BIÊN MỤC");
        System.out.println("════════════════════════════════════════════════════════════");
        System.out.printf("  %-35s %-12s %-8s %-6s%n",
                "Nhan đề", "Ký hiệu xếp giá", "Kho", "Bản");
        System.out.println("  " + "─".repeat(65));

        for (Document doc : docs) {
            System.out.printf("  %-35s %-12s %-8s %-6d%n",
                    doc.getTitle().length() > 33
                            ? doc.getTitle().substring(0, 30) + "..."
                            : doc.getTitle(),
                    doc.getCallNumber(),
                    doc.getWarehouseLocation().getCode(),
                    doc.getTotalCopies());
        }

        long totalCopies = docs.stream().mapToLong(Document::getTotalCopies).sum();
        System.out.println("  " + "─".repeat(65));
        System.out.printf("  Tổng cộng: %d đầu sách | %d bản sao%n", docs.size(), totalCopies);
        System.out.println("════════════════════════════════════════════════════════════");
    }
}