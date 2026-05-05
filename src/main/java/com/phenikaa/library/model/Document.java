package com.phenikaa.library.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DOCUMENT (Tài liệu / Đầu sách)
 *
 * Đại diện cho một đầu sách trong hệ thống biên mục Phenikaa.
 * Một Document có thể có nhiều PhysicalCopy (bản sao vật lý).
 *
 * Quy trình: Bước 1 - Tiếp nhận tài liệu mới (BM.01.QT.02.TV.NV)
 */
public class Document {

    // ─── Định danh ────────────────────────────────────────────────────────────
    private Long id;
    private String isbn;              // ISBN-10 hoặc ISBN-13

    // ─── Thông tin mô tả (Descriptive Metadata) ───────────────────────────────
    private String title;             // Nhan đề chính
    private String parallelTitle;     // Nhan đề song song (tiếng Anh nếu có)
    private String author;            // Tác giả chính
    private String coAuthors;         // Đồng tác giả (phân cách bởi ";")
    private String publisher;         // Nhà xuất bản
    private String placeOfPublication;// Nơi xuất bản
    private int    publishYear;       // Năm xuất bản
    private int    edition;           // Lần xuất bản
    private int    totalPages;        // Số trang
    private String language;          // Ngôn ngữ: "vi", "en", ...
    private String series;            // Tên bộ sách (nếu có)
    private String summary;           // Tóm tắt nội dung

    // ─── Phân loại & Biên mục (Classification & Cataloging) ──────────────────
    private String ddcCode;           // Mã DDC 23, vd: "005.133"
    private String ddcClassName;      // Tên lớp DDC, vd: "Lập trình máy tính"
    private String cutterNumber;      // Chỉ số Cutter theo nhan đề, vd: "N489"
    private String callNumber;        // Ký hiệu xếp giá = DDC + Cutter + Năm
    private String subjectHeading;    // Đề mục chủ đề (từ khoá)
    private String keywords;          // Từ khoá tìm kiếm (phân cách ";")

    // ─── Loại tài liệu ────────────────────────────────────────────────────────
    public enum DocumentType {
        GIAO_TRINH("GT", "Giáo trình"),
        TAI_LIEU_THAM_KHAO("TK", "Tài liệu tham khảo"),
        LUAN_VAN("LV", "Luận văn"),
        LUAN_AN("LA", "Luận án"),
        BAO_CAO_KHOA_HOC("BC", "Báo cáo khoa học"),
        TAP_CHI("TC", "Tạp chí"),
        SACH_THAM_KHAO("ST", "Sách tham khảo");

        private final String code;
        private final String label;

        DocumentType(String code, String label) {
            this.code  = code;
            this.label = label;
        }

        public String getCode()  { return code;  }
        public String getLabel() { return label; }
    }

    private DocumentType documentType;

    // ─── Kho lưu trữ ──────────────────────────────────────────────────────────
    public enum WarehouseLocation {
        PHONG_DOC_MO("PDM", "Phòng đọc mở"),
        PHONG_BAO_QUAN("PBQ", "Phòng bảo quản"),
        KHO_GIAO_TRINH("KGT", "Kho giáo trình"),
        KHO_LUAN_VAN("KLV", "Kho luận văn");

        private final String code;
        private final String label;

        WarehouseLocation(String code, String label) {
            this.code  = code;
            this.label = label;
        }

        public String getCode()  { return code;  }
        public String getLabel() { return label; }
    }

    private WarehouseLocation warehouseLocation;

    // ─── Thông tin nghiệp vụ thư viện ─────────────────────────────────────────
    private LocalDate receivedDate;   // Ngày nhận tài liệu (Bước 1)
    private LocalDate catalogedDate;  // Ngày biên mục hoàn thành
    private String    catalogedBy;    // CBTV thực hiện biên mục
    private String    sourceDocument; // Số phiếu nhập kho (BM.01.QT.02.TV.NV)
    private double    unitPrice;      // Đơn giá (từ phiếu nhập kho)

    // ─── Bản sao vật lý ───────────────────────────────────────────────────────
    private List<PhysicalCopy> copies = new ArrayList<>();

    // ─── Constructor ──────────────────────────────────────────────────────────
    public Document() {}

    public Document(String isbn, String title, String author,
                    String publisher, int publishYear) {
        this.isbn        = isbn;
        this.title       = title;
        this.author      = author;
        this.publisher   = publisher;
        this.publishYear = publishYear;
        this.receivedDate = LocalDate.now();
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────
    public Long getId()                    { return id; }
    public void setId(Long id)             { this.id = id; }

    public String getIsbn()                { return isbn; }
    public void setIsbn(String isbn)       { this.isbn = isbn; }

    public String getTitle()               { return title; }
    public void setTitle(String title)     { this.title = title; }

    public String getParallelTitle()                       { return parallelTitle; }
    public void setParallelTitle(String parallelTitle)     { this.parallelTitle = parallelTitle; }

    public String getAuthor()              { return author; }
    public void setAuthor(String author)   { this.author = author; }

    public String getCoAuthors()                   { return coAuthors; }
    public void setCoAuthors(String coAuthors)     { this.coAuthors = coAuthors; }

    public String getPublisher()                   { return publisher; }
    public void setPublisher(String publisher)     { this.publisher = publisher; }

    public String getPlaceOfPublication()                              { return placeOfPublication; }
    public void setPlaceOfPublication(String placeOfPublication)       { this.placeOfPublication = placeOfPublication; }

    public int getPublishYear()                    { return publishYear; }
    public void setPublishYear(int publishYear)    { this.publishYear = publishYear; }

    public int getEdition()                { return edition; }
    public void setEdition(int edition)    { this.edition = edition; }

    public int getTotalPages()                     { return totalPages; }
    public void setTotalPages(int totalPages)      { this.totalPages = totalPages; }

    public String getLanguage()                    { return language; }
    public void setLanguage(String language)       { this.language = language; }

    public String getSeries()              { return series; }
    public void setSeries(String series)   { this.series = series; }

    public String getSummary()             { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getDdcCode()             { return ddcCode; }
    public void setDdcCode(String ddcCode) { this.ddcCode = ddcCode; }

    public String getDdcClassName()                        { return ddcClassName; }
    public void setDdcClassName(String ddcClassName)       { this.ddcClassName = ddcClassName; }

    public String getCutterNumber()                        { return cutterNumber; }
    public void setCutterNumber(String cutterNumber)       { this.cutterNumber = cutterNumber; }

    public String getCallNumber()                          { return callNumber; }
    public void setCallNumber(String callNumber)           { this.callNumber = callNumber; }

    public String getSubjectHeading()                      { return subjectHeading; }
    public void setSubjectHeading(String subjectHeading)   { this.subjectHeading = subjectHeading; }

    public String getKeywords()                    { return keywords; }
    public void setKeywords(String keywords)       { this.keywords = keywords; }

    public DocumentType getDocumentType()                          { return documentType; }
    public void setDocumentType(DocumentType documentType)         { this.documentType = documentType; }

    public WarehouseLocation getWarehouseLocation()                            { return warehouseLocation; }
    public void setWarehouseLocation(WarehouseLocation warehouseLocation)      { this.warehouseLocation = warehouseLocation; }

    public LocalDate getReceivedDate()                     { return receivedDate; }
    public void setReceivedDate(LocalDate receivedDate)    { this.receivedDate = receivedDate; }

    public LocalDate getCatalogedDate()                    { return catalogedDate; }
    public void setCatalogedDate(LocalDate catalogedDate)  { this.catalogedDate = catalogedDate; }

    public String getCatalogedBy()                         { return catalogedBy; }
    public void setCatalogedBy(String catalogedBy)         { this.catalogedBy = catalogedBy; }

    public String getSourceDocument()                      { return sourceDocument; }
    public void setSourceDocument(String sourceDocument)   { this.sourceDocument = sourceDocument; }

    public double getUnitPrice()                   { return unitPrice; }
    public void setUnitPrice(double unitPrice)     { this.unitPrice = unitPrice; }

    public List<PhysicalCopy> getCopies()          { return copies; }
    public void setCopies(List<PhysicalCopy> c)    { this.copies = c; }

    public void addCopy(PhysicalCopy copy) {
        copy.setDocument(this);
        this.copies.add(copy);
    }

    // ─── Tiện ích ─────────────────────────────────────────────────────────────
    public int getTotalCopies() {
        return copies.size();
    }

    public long getAvailableCopies() {
        return copies.stream()
                .filter(c -> c.getStatus() == PhysicalCopy.CopyStatus.AVAILABLE)
                .count();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s / %s - %s, %d | DDC: %s | CN: %s",
                isbn, title, author, publisher, publishYear, ddcCode, callNumber);
    }
}