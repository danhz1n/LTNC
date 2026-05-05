package com.phenikaa.library.service;

import com.phenikaa.library.model.Document;
import com.phenikaa.library.model.Document.DocumentType;
import com.phenikaa.library.model.PhysicalCopy;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ACCESSION SERVICE (Dịch vụ cấp số ĐKCB và Mã vạch)
 *
 * Thực hiện Bước 2 của quy trình QT.02.TV.NV:
 *   - Phân kho (quyết định kho nào chứa tài liệu)
 *   - Đóng dấu thư viện + dấu số ĐKCB
 *   - Sinh mã vạch theo mẫu phần mềm TV Koha của Phenikaa
 *   - Sinh số Đăng Ký Cá Biệt (ĐKCB)
 *
 * Quy tắc số ĐKCB Phenikaa:
 *   - Giáo trình:      GT.YYYY.NNNNNN  (vd: GT.2024.000123)
 *   - Tài liệu TK:     TK.YYYY.NNNNNN  (vd: TK.2024.000456)
 *   - Luận văn:        LV.YYYY.NNNNNN
 *   - Luận án:         LA.YYYY.NNNNNN
 *   - Báo cáo KH:      BC.YYYY.NNNNNN
 *   - Tạp chí:         TC.YYYY.NNNNNN
 *
 * Quy tắc mã vạch:
 *   - Format: PKA-YYYYMMDD-NNNNNN  (Phenikaa + Ngày + Số TT)
 *   - Ví dụ:  PKA-20240322-001234
 *
 * Thời gian chuẩn: 3 phút/tài liệu (theo bảng lưu đồ Phenikaa)
 */
public class AccessionService {

    private static final String LIBRARY_CODE    = "PKA";  // Phenikaa University Library
    private static final String LIBRARY_NAME    = "THƯ VIỆN TRƯỜNG ĐẠI HỌC PHENIKAA";

    // Bộ đếm tuần tự (trong thực tế lấy từ DB)
    private final AtomicLong barcodeSequence    = new AtomicLong(1000);
    private final AtomicLong accessionSeqGT     = new AtomicLong(1);   // Giáo trình
    private final AtomicLong accessionSeqTK     = new AtomicLong(1);   // Tài liệu TK
    private final AtomicLong accessionSeqLV     = new AtomicLong(1);   // Luận văn
    private final AtomicLong accessionSeqLA     = new AtomicLong(1);   // Luận án
    private final AtomicLong accessionSeqBC     = new AtomicLong(1);   // Báo cáo KH
    private final AtomicLong accessionSeqTC     = new AtomicLong(1);   // Tạp chí
    private final AtomicLong accessionSeqOther  = new AtomicLong(1);   // Khác

    // ─── Bước 2: Phân kho, đóng dấu, dán mã vạch ────────────────────────────

    /**
     * Thực hiện toàn bộ Bước 2 cho một bản sao vật lý.
     * Gồm: phân kho → đóng dấu TV → đóng dấu ĐKCB → dán mã vạch
     */
    public void processStep2(PhysicalCopy copy, String staffName) {
        Document doc = copy.getDocument();

        System.out.println("\n── BƯỚC 2: PHÂN KHO, ĐÓNG DẤU, DÁN MÃ VẠCH ───────────");
        System.out.printf("  Tài liệu: %s | Bản: %d%n", doc.getTitle(), copy.getCopyNumber());

        // 2a. Sinh số ĐKCB
        String dkcb = generateAccessionNumber(doc.getDocumentType());
        copy.setAccessionNumber(dkcb);
        System.out.printf("  → Số ĐKCB: %s%n", dkcb);

        // 2b. Sinh mã vạch (theo mẫu phần mềm Koha của Phenikaa)
        String barcode = generateBarcode();
        copy.setBarcode(barcode);
        System.out.printf("  → Mã vạch: %s%n", barcode);

        // 2c. Đóng dấu thư viện (mô phỏng)
        copy.setLibraryStampApplied(true);
        System.out.println("  → Đóng dấu thư viện: ✓");

        // 2d. Đóng dấu số ĐKCB
        copy.setDkcbStampApplied(true);
        System.out.printf("  → Đóng dấu ĐKCB (%s): ✓%n", dkcb);

        copy.setProcessedBy(staffName);
        System.out.println("  ✓ Phân kho – Đóng dấu – Mã vạch HOÀN THÀNH");
    }

    // ─── Sinh số ĐKCB ─────────────────────────────────────────────────────────

    /**
     * Sinh số Đăng Ký Cá Biệt (ĐKCB) theo loại tài liệu
     *
     * Format: {LOẠI}.{YYYY}.{NNNNNN}
     * Ví dụ:  GT.2024.000123
     */
    public String generateAccessionNumber(DocumentType type) {
        int year = LocalDate.now().getYear();
        String prefix;
        long seq;

        if (type == null) {
            prefix = "TL";
            seq = accessionSeqOther.getAndIncrement();
        } else {
            switch (type) {
                case GIAO_TRINH:
                    prefix = "GT"; seq = accessionSeqGT.getAndIncrement(); break;
                case TAI_LIEU_THAM_KHAO:
                case SACH_THAM_KHAO:
                    prefix = "TK"; seq = accessionSeqTK.getAndIncrement(); break;
                case LUAN_VAN:
                    prefix = "LV"; seq = accessionSeqLV.getAndIncrement(); break;
                case LUAN_AN:
                    prefix = "LA"; seq = accessionSeqLA.getAndIncrement(); break;
                case BAO_CAO_KHOA_HOC:
                    prefix = "BC"; seq = accessionSeqBC.getAndIncrement(); break;
                case TAP_CHI:
                    prefix = "TC"; seq = accessionSeqTC.getAndIncrement(); break;
                default:
                    prefix = "TL"; seq = accessionSeqOther.getAndIncrement(); break;
            }
        }

        return String.format("%s.%d.%06d", prefix, year, seq);
    }

    // ─── Sinh mã vạch ─────────────────────────────────────────────────────────

    /**
     * Sinh mã vạch duy nhất theo chuẩn phần mềm Koha của Phenikaa
     *
     * Format: PKA-YYYYMMDD-NNNNNN
     * Ví dụ:  PKA-20240322-001234
     */
    public String generateBarcode() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seq = barcodeSequence.getAndIncrement();
        return String.format("%s-%s-%06d", LIBRARY_CODE, date, seq);
    }

    // ─── Validate ISBN ─────────────────────────────────────────────────────────

    /**
     * Kiểm tra tính hợp lệ của ISBN-13
     * Thuật toán: tổng trọng số phải chia hết cho 10
     */
    public boolean validateIsbn13(String isbn) {
        if (isbn == null) return false;
        String clean = isbn.replaceAll("[\\s\\-]", "");
        if (clean.length() != 13 || !clean.matches("\\d+")) return false;

        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(clean.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int checkDigit = (10 - (sum % 10)) % 10;
        return checkDigit == Character.getNumericValue(clean.charAt(12));
    }

    /**
     * Kiểm tra tính hợp lệ của ISBN-10
     */
    public boolean validateIsbn10(String isbn) {
        if (isbn == null) return false;
        String clean = isbn.replaceAll("[\\s\\-]", "");
        if (clean.length() != 10) return false;

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            if (!Character.isDigit(clean.charAt(i))) return false;
            sum += (i + 1) * Character.getNumericValue(clean.charAt(i));
        }
        char last = clean.charAt(9);
        sum += (last == 'X' || last == 'x') ? 100 : 10 * Character.getNumericValue(last);
        return sum % 11 == 0;
    }

    /**
     * Validate ISBN (tự động phát hiện ISBN-10 hay ISBN-13)
     */
    public boolean validateIsbn(String isbn) {
        if (isbn == null) return false;
        String clean = isbn.replaceAll("[\\s\\-]", "");
        if (clean.length() == 13) return validateIsbn13(isbn);
        if (clean.length() == 10) return validateIsbn10(isbn);
        return false;
    }

    /**
     * Chuyển ISBN-10 sang ISBN-13
     */
    public String isbn10ToIsbn13(String isbn10) {
        String clean = isbn10.replaceAll("[\\s\\-]", "").substring(0, 9);
        String base  = "978" + clean;
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int d = Character.getNumericValue(base.charAt(i));
            sum += (i % 2 == 0) ? d : d * 3;
        }
        int check = (10 - (sum % 10)) % 10;
        return base + check;
    }

    // ─── Thông tin thư viện ────────────────────────────────────────────────────

    public String getLibraryCode() { return LIBRARY_CODE; }
    public String getLibraryName() { return LIBRARY_NAME; }

    /**
     * Khởi tạo sequence từ giá trị lưu trong DB (gọi khi khởi động ứng dụng)
     */
    public void initSequences(long bcSeq, long gtSeq, long tkSeq,
                               long lvSeq, long laSeq, long barcodeSeq) {
        barcodeSequence.set(barcodeSeq);
        accessionSeqGT.set(gtSeq);
        accessionSeqTK.set(tkSeq);
        accessionSeqLV.set(lvSeq);
        accessionSeqLA.set(laSeq);
    }
}