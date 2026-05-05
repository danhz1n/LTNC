package com.phenikaa.library.service;

import com.phenikaa.library.model.Document;
import com.phenikaa.library.model.PhysicalCopy;

import java.time.LocalDate;
import java.util.List;

/**
 * LABEL PRINTER (In nhãn và dán gáy cho tài liệu)
 *
 * Thực hiện Bước 4 của quy trình QT.02.TV.NV:
 *   - Tạo nội dung nhãn gáy (spine label)
 *   - In nhãn gáy cho từng bản sao
 *   - Ghi nhận trạng thái đã in / đã dán nhãn
 *
 * Thời gian chuẩn: 3 phút/tài liệu (theo bảng lưu đồ Phenikaa)
 *
 * Nhãn gáy chuẩn gồm 3 dòng:
 *   Dòng 1: Mã DDC
 *   Dòng 2: Chỉ số Cutter
 *   Dòng 3: Năm xuất bản
 *
 * Ví dụ nhãn gáy:
 *   ┌──────────┐
 *   │ 005.133  │
 *   │  N489    │
 *   │   2023   │
 *   └──────────┘
 */
public class LabelPrinter {

    private static final int LABEL_WIDTH = 12;  // Chiều rộng nhãn (ký tự)

    // ─── Bước 4: In nhãn và dán gáy ──────────────────────────────────────────

    /**
     * Thực hiện Bước 4 cho tất cả bản sao của một tài liệu.
     * In nhãn gáy và ghi nhận trạng thái.
     */
    public void processStep4(Document doc, String staffName) {
        System.out.println("\n── BƯỚC 4: IN NHÃN VÀ DÁN GÁY CHO TÀI LIỆU ──────────");
        System.out.printf("  Tài liệu: %s (%d bản)%n",
                doc.getTitle(), doc.getCopies().size());

        for (PhysicalCopy copy : doc.getCopies()) {
            printSpineLabel(copy);
            copy.setSpineLabelPrinted(true);
            copy.setSpineLabelApplied(true);
            copy.setProcessedDate(LocalDate.now());
            System.out.printf("  ✓ Bản %d – Mã vạch %s – Nhãn gáy đã in và dán%n",
                    copy.getCopyNumber(), copy.getBarcode());
        }

        System.out.println("  ✓ In nhãn – Dán gáy HOÀN THÀNH");
    }

    // ─── Sinh nhãn gáy ────────────────────────────────────────────────────────

    /**
     * Sinh và in nhãn gáy ra console (mô phỏng máy in nhãn).
     *
     * Trong thực tế, method này sẽ:
     *   1. Tạo file PDF/ZPL gửi đến máy in nhãn (Brady, Zebra, Dymo...)
     *   2. Hoặc gọi API phần mềm Koha để in nhãn
     */
    public void printSpineLabel(PhysicalCopy copy) {
        Document doc = copy.getDocument();
        String labelContent = buildSpineLabel(
                doc.getDdcCode(),
                doc.getCutterNumber(),
                doc.getPublishYear()
        );
        copy.setSpineLabel(labelContent);

        System.out.println("  ┌" + "─".repeat(LABEL_WIDTH) + "┐");
        for (String line : labelContent.split("\n")) {
            System.out.printf("  │%-" + LABEL_WIDTH + "s│%n", center(line, LABEL_WIDTH));
        }
        System.out.println("  └" + "─".repeat(LABEL_WIDTH) + "┘");
        System.out.printf("  BC: %s%n", copy.getBarcode());
    }

    /**
     * Xây dựng nội dung nhãn gáy (3 dòng chuẩn)
     *
     * @param ddcCode    Mã DDC, vd: "005.133"
     * @param cutter     Chỉ số Cutter, vd: "N489"
     * @param year       Năm xuất bản, vd: 2023
     * @return Chuỗi 3 dòng, ngăn cách bởi "\n"
     */
    public String buildSpineLabel(String ddcCode, String cutter, int year) {
        return String.format("%s\n%s\n%d",
                ddcCode  != null ? ddcCode  : "???",
                cutter   != null ? cutter   : "???",
                year);
    }

    // ─── Nhãn mã vạch (Barcode label) ────────────────────────────────────────

    /**
     * In nhãn mã vạch đầy đủ (dán vào trang đầu sách / bìa trong).
     *
     * Nội dung gồm:
     *   - Tên thư viện
     *   - Nhan đề sách (rút gọn)
     *   - Số ĐKCB
     *   - Mã vạch (dạng text, thực tế sẽ render barcode image)
     *   - Ký hiệu xếp giá
     */
    public void printBarcodeLabel(PhysicalCopy copy) {
        Document doc = copy.getDocument();
        String title = doc.getTitle().length() > 35
                ? doc.getTitle().substring(0, 32) + "..."
                : doc.getTitle();

        System.out.println("  ╔" + "═".repeat(40) + "╗");
        System.out.printf("  ║ %-38s ║%n", "THƯ VIỆN TRƯỜNG ĐH PHENIKAA");
        System.out.println("  ║" + "─".repeat(40) + "║");
        System.out.printf("  ║ %-38s ║%n", title);
        System.out.printf("  ║ ĐKCB: %-33s ║%n", copy.getAccessionNumber());
        System.out.printf("  ║ %s ║%n", renderBarcodeAscii(copy.getBarcode()));
        System.out.printf("  ║ %-38s ║%n", copy.getBarcode());
        System.out.printf("  ║ CN: %-35s ║%n", doc.getCallNumber());
        System.out.println("  ╚" + "═".repeat(40) + "╝");
    }

    /**
     * Mô phỏng mã vạch ASCII (trong thực tế dùng thư viện ZXing/Barcode4J)
     */
    private String renderBarcodeAscii(String data) {
        // Mô phỏng đơn giản — trong production dùng:
        // com.google.zxing.BarcodeFormat.CODE_128
        return "│█▌│▌█│██│▌█│▌│█▌│██│▌│█▌│";
    }

    // ─── Phiếu thông báo sách mới (BM.04.QT.02.TV.NV) ────────────────────────

    /**
     * In thông báo sách mới (BM.04.QT.02.TV.NV)
     * Gọi sau khi tài liệu đã được xử lý xong và giao về kho.
     */
    public void printNewBookNotice(List<Document> newBooks, String month, int year) {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║     TRƯỜNG ĐẠI HỌC PHENIKAA – THƯ VIỆN              ║");
        System.out.printf ("║   THÔNG BÁO SÁCH MỚI THÁNG %02d NĂM %d             ║%n", month.isEmpty() ? 0 : Integer.parseInt(month), year);
        System.out.println("╠═══╦═══════════════════════════════╦═══════════╦══════╣");
        System.out.println("║STT║ Tên sách                      ║ Tác giả   ║ ĐKCB ║");
        System.out.println("╠═══╬═══════════════════════════════╬═══════════╬══════╣");

        int i = 1;
        for (Document doc : newBooks) {
            String title  = doc.getTitle().length() > 29
                    ? doc.getTitle().substring(0, 26) + "..."
                    : doc.getTitle();
            String author = doc.getAuthor() != null && doc.getAuthor().length() > 9
                    ? doc.getAuthor().substring(0, 8) + "."
                    : (doc.getAuthor() != null ? doc.getAuthor() : "");
            String dkcb = doc.getCopies().isEmpty() ? ""
                    : doc.getCopies().get(0).getAccessionNumber();

            System.out.printf("║%3d║ %-29s ║ %-9s ║ %-4s ║%n",
                    i++, title, author, dkcb);
        }

        System.out.println("╚═══╩═══════════════════════════════╩═══════════╩══════╝");
        System.out.printf("Mã biểu mẫu: BM.04.QT.02.TV.NV | Ngày in: %s%n",
                LocalDate.now());
    }

    // ─── Tiện ích ─────────────────────────────────────────────────────────────

    private String center(String text, int width) {
        if (text == null) text = "";
        if (text.length() >= width) return text.substring(0, width);
        int pad  = width - text.length();
        int left = pad / 2;
        int right = pad - left;
        return " ".repeat(left) + text + " ".repeat(right);
    }
}