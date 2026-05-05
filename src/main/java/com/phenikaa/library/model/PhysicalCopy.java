package com.phenikaa.library.model;

import java.time.LocalDate;

/**
 * PHYSICAL COPY (Bản sao vật lý)
 *
 * Đại diện cho một cuốn sách vật lý cụ thể trong kho.
 * Mỗi bản có mã vạch riêng và số ĐKCB riêng.
 *
 * Quy trình:
 *   Bước 2 - Phân kho, đóng dấu, dán mã vạch
 *   Bước 4 - In nhãn và dán gáy cho tài liệu
 *   Bước 5 - Giao tài liệu về các kho để xếp giá
 */
public class PhysicalCopy {

    // ─── Định danh ────────────────────────────────────────────────────────────
    private Long   id;
    private String barcode;           // Mã vạch dán lên sách (sinh từ phần mềm Koha)
    private String accessionNumber;   // Số ĐKCB (Đăng Ký Cá Biệt) - Sổ ĐKCB

    // ─── Tham chiếu đầu sách ──────────────────────────────────────────────────
    private Document document;        // Đầu sách tương ứng
    private int      copyNumber;      // Bản thứ mấy của đầu sách này

    // ─── Vị trí vật lý ────────────────────────────────────────────────────────
    private String shelfLocation;     // Vị trí giá sách, vd: "A1-3", "B2-5"
    private String roomCode;          // Mã phòng/kho

    // ─── Trạng thái ───────────────────────────────────────────────────────────
    public enum CopyStatus {
        AVAILABLE  ("Có sẵn"),
        BORROWED   ("Đang mượn"),
        RESERVED   ("Đã đặt trước"),
        PROCESSING ("Đang xử lý nghiệp vụ"),   // Đang trong quy trình biên mục
        DAMAGED    ("Hư hỏng"),
        LOST       ("Mất"),
        WITHDRAWN  ("Thanh lý");

        private final String label;
        CopyStatus(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private CopyStatus status = CopyStatus.PROCESSING;

    // ─── Nhãn gáy sách ────────────────────────────────────────────────────────
    /**
     * Nhãn gáy (spine label) in ra và dán lên gáy sách.
     * Bước 4: In nhãn và dán gáy cho tài liệu.
     *
     * Ví dụ:
     *   ┌──────────┐
     *   │ 005.133  │  ← DDC code
     *   │  N489    │  ← Cutter number
     *   │   2023   │  ← Năm xuất bản
     *   └──────────┘
     */
    private String spineLabel;        // Nội dung nhãn gáy
    private boolean spineLabelPrinted = false;  // Đã in nhãn gáy chưa?
    private boolean spineLabelApplied = false;  // Đã dán nhãn gáy chưa?

    // ─── Đóng dấu thư viện ────────────────────────────────────────────────────
    private boolean libraryStampApplied = false;  // Đóng dấu thư viện
    private boolean dkcbStampApplied    = false;  // Đóng dấu số ĐKCB

    // ─── Lịch sử ──────────────────────────────────────────────────────────────
    private LocalDate receivedDate;       // Ngày nhận (Bước 1)
    private LocalDate processedDate;      // Ngày xử lý nghiệp vụ xong (Bước 3)
    private LocalDate deliveredDate;      // Ngày giao về kho (Bước 5)
    private String    processedBy;        // NV nghiệp vụ thực hiện

    // ─── Constructor ──────────────────────────────────────────────────────────
    public PhysicalCopy() {}

    public PhysicalCopy(Document document, int copyNumber) {
        this.document    = document;
        this.copyNumber  = copyNumber;
        this.receivedDate = LocalDate.now();
        this.status      = CopyStatus.PROCESSING;
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────
    public Long getId()                  { return id; }
    public void setId(Long id)           { this.id = id; }

    public String getBarcode()               { return barcode; }
    public void setBarcode(String barcode)   { this.barcode = barcode; }

    public String getAccessionNumber()                     { return accessionNumber; }
    public void setAccessionNumber(String accessionNumber) { this.accessionNumber = accessionNumber; }

    public Document getDocument()                  { return document; }
    public void setDocument(Document document)     { this.document = document; }

    public int getCopyNumber()                     { return copyNumber; }
    public void setCopyNumber(int copyNumber)      { this.copyNumber = copyNumber; }

    public String getShelfLocation()                       { return shelfLocation; }
    public void setShelfLocation(String shelfLocation)     { this.shelfLocation = shelfLocation; }

    public String getRoomCode()                    { return roomCode; }
    public void setRoomCode(String roomCode)       { this.roomCode = roomCode; }

    public CopyStatus getStatus()                  { return status; }
    public void setStatus(CopyStatus status)       { this.status = status; }

    public String getSpineLabel()                          { return spineLabel; }
    public void setSpineLabel(String spineLabel)           { this.spineLabel = spineLabel; }

    public boolean isSpineLabelPrinted()                           { return spineLabelPrinted; }
    public void setSpineLabelPrinted(boolean spineLabelPrinted)    { this.spineLabelPrinted = spineLabelPrinted; }

    public boolean isSpineLabelApplied()                           { return spineLabelApplied; }
    public void setSpineLabelApplied(boolean spineLabelApplied)    { this.spineLabelApplied = spineLabelApplied; }

    public boolean isLibraryStampApplied()                                 { return libraryStampApplied; }
    public void setLibraryStampApplied(boolean libraryStampApplied)        { this.libraryStampApplied = libraryStampApplied; }

    public boolean isDkcbStampApplied()                            { return dkcbStampApplied; }
    public void setDkcbStampApplied(boolean dkcbStampApplied)      { this.dkcbStampApplied = dkcbStampApplied; }

    public LocalDate getReceivedDate()                     { return receivedDate; }
    public void setReceivedDate(LocalDate receivedDate)    { this.receivedDate = receivedDate; }

    public LocalDate getProcessedDate()                    { return processedDate; }
    public void setProcessedDate(LocalDate processedDate)  { this.processedDate = processedDate; }

    public LocalDate getDeliveredDate()                    { return deliveredDate; }
    public void setDeliveredDate(LocalDate deliveredDate)  { this.deliveredDate = deliveredDate; }

    public String getProcessedBy()                         { return processedBy; }
    public void setProcessedBy(String processedBy)         { this.processedBy = processedBy; }

    // ─── Tiện ích ─────────────────────────────────────────────────────────────

    /** Kiểm tra bản sao đã hoàn thành toàn bộ quy trình nghiệp vụ chưa */
    public boolean isFullyProcessed() {
        return barcode           != null
            && accessionNumber   != null
            && spineLabel        != null
            && spineLabelPrinted
            && spineLabelApplied
            && libraryStampApplied
            && dkcbStampApplied;
    }

    /** Tóm tắt trạng thái xử lý nghiệp vụ */
    public String getProcessingStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append(barcode           != null ? "✓" : "✗").append(" Mã vạch | ");
        sb.append(accessionNumber   != null ? "✓" : "✗").append(" ĐKCB | ");
        sb.append(libraryStampApplied       ? "✓" : "✗").append(" Dấu TV | ");
        sb.append(dkcbStampApplied          ? "✓" : "✗").append(" Dấu ĐKCB | ");
        sb.append(spineLabelPrinted         ? "✓" : "✗").append(" In nhãn | ");
        sb.append(spineLabelApplied         ? "✓" : "✗").append(" Dán nhãn");
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("Bản %d | BC: %s | ĐKCB: %s | %s | %s",
                copyNumber, barcode, accessionNumber,
                shelfLocation != null ? shelfLocation : "Chưa xếp giá",
                status.getLabel());
    }
}