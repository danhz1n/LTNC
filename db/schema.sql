-- ============================================================
-- SCHEMA.SQL – Hệ thống Biên mục Tài liệu Thư viện Phenikaa
-- Quy trình: QT.02.TV.NV
-- ============================================================

-- ─── Bảng tài liệu (Đầu sách) ──────────────────────────────
CREATE TABLE documents (
    id                   BIGINT PRIMARY KEY AUTO_INCREMENT,

    -- Định danh
    isbn                 VARCHAR(20)   UNIQUE,

    -- Thông tin mô tả
    title                VARCHAR(500)  NOT NULL,
    parallel_title       VARCHAR(500),
    author               VARCHAR(300)  NOT NULL,
    co_authors           VARCHAR(500),
    publisher            VARCHAR(200),
    place_of_publication VARCHAR(100),
    publish_year         INT           NOT NULL,
    edition              INT           DEFAULT 1,
    total_pages          INT,
    language             VARCHAR(10)   DEFAULT 'vi',
    series               VARCHAR(200),
    summary              TEXT,

    -- Phân loại DDC 23
    ddc_code             VARCHAR(20),   -- vd: 005.133
    ddc_class_name       VARCHAR(200),  -- vd: Ngôn ngữ lập trình
    cutter_number        VARCHAR(20),   -- vd: N489
    call_number          VARCHAR(50),   -- vd: 005.133 N489 2023
    subject_heading      VARCHAR(500),
    keywords             VARCHAR(500),

    -- Loại tài liệu & kho
    document_type        VARCHAR(30)   NOT NULL,
    -- GT, TK, LV, LA, BC, TC
    warehouse_location   VARCHAR(10),
    -- PDM, PBQ, KGT, KLV

    -- Thông tin nghiệp vụ (Bước 1)
    received_date        DATE,
    cataloged_date       DATE,
    cataloged_by         VARCHAR(100),
    source_document      VARCHAR(50),   -- Số phiếu nhập kho
    unit_price           DECIMAL(15,0)  DEFAULT 0,

    created_at           TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_title       (title(100)),
    INDEX idx_author      (author(100)),
    INDEX idx_ddc         (ddc_code),
    INDEX idx_call_number (call_number),
    INDEX idx_doc_type    (document_type)
);

-- ─── Bảng bản sao vật lý (Bước 2, 4, 5) ────────────────────
CREATE TABLE physical_copies (
    id                   BIGINT PRIMARY KEY AUTO_INCREMENT,
    document_id          BIGINT        NOT NULL,

    -- Định danh (Bước 2)
    barcode              VARCHAR(30)   UNIQUE NOT NULL,
    accession_number     VARCHAR(30)   UNIQUE NOT NULL,  -- Số ĐKCB
    copy_number          INT           NOT NULL,

    -- Vị trí vật lý (Bước 5)
    shelf_location       VARCHAR(20),   -- vd: A1-3
    room_code            VARCHAR(10),   -- vd: PDM

    -- Trạng thái
    status               VARCHAR(20)   DEFAULT 'PROCESSING',
    -- PROCESSING, AVAILABLE, BORROWED, RESERVED, DAMAGED, LOST, WITHDRAWN

    -- Đóng dấu (Bước 2)
    library_stamp_applied   BOOLEAN   DEFAULT FALSE,
    dkcb_stamp_applied      BOOLEAN   DEFAULT FALSE,

    -- Nhãn gáy (Bước 4)
    spine_label          VARCHAR(50),
    spine_label_printed  BOOLEAN       DEFAULT FALSE,
    spine_label_applied  BOOLEAN       DEFAULT FALSE,

    -- Lịch sử xử lý
    received_date        DATE,
    processed_date       DATE,
    delivered_date       DATE,
    processed_by         VARCHAR(100),

    created_at           TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE,
    INDEX idx_barcode      (barcode),
    INDEX idx_accession    (accession_number),
    INDEX idx_doc_copies   (document_id),
    INDEX idx_status       (status)
);

-- ─── Sổ ĐKCB (Accession Register) ───────────────────────────
-- Tương ứng với Sổ Đăng Ký Cá Biệt lưu lâu dài tại thư viện
CREATE TABLE accession_register (
    id                   BIGINT PRIMARY KEY AUTO_INCREMENT,
    accession_number     VARCHAR(30)   UNIQUE NOT NULL,
    document_id          BIGINT        NOT NULL,
    copy_id              BIGINT        NOT NULL,
    title                VARCHAR(500),
    author               VARCHAR(300),
    publisher            VARCHAR(200),
    publish_year         INT,
    document_type        VARCHAR(30),
    warehouse_location   VARCHAR(10),
    unit_price           DECIMAL(15,0),
    registered_date      DATE          NOT NULL,
    registered_by        VARCHAR(100),
    notes                TEXT,

    FOREIGN KEY (document_id) REFERENCES documents(id),
    FOREIGN KEY (copy_id)     REFERENCES physical_copies(id)
);

-- ─── Phiếu nhập kho (BM.01.QT.02.TV.NV) ─────────────────────
CREATE TABLE import_receipts (
    id                   BIGINT PRIMARY KEY AUTO_INCREMENT,
    receipt_number       VARCHAR(50)   UNIQUE NOT NULL,   -- Số phiếu
    receipt_date         DATE          NOT NULL,
    supplier_name        VARCHAR(200),
    delivery_person      VARCHAR(100),
    warehouse_in         VARCHAR(50),
    total_amount         DECIMAL(15,0),
    total_items          INT,
    notes                TEXT,
    created_by           VARCHAR(100),
    created_at           TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

-- Dòng chi tiết của phiếu nhập kho
CREATE TABLE import_receipt_items (
    id                   BIGINT PRIMARY KEY AUTO_INCREMENT,
    receipt_id           BIGINT        NOT NULL,
    document_id          BIGINT,
    item_name            VARCHAR(500)  NOT NULL,
    item_code            VARCHAR(50),
    unit                 VARCHAR(20)   DEFAULT 'cuốn',
    quantity_ordered     INT,
    quantity_received    INT,
    unit_price           DECIMAL(15,0),
    total_price          DECIMAL(15,0),

    FOREIGN KEY (receipt_id)  REFERENCES import_receipts(id),
    FOREIGN KEY (document_id) REFERENCES documents(id)
);

-- ─── Biên bản bàn giao (BM.03.QT.02.TV.NV) ──────────────────
CREATE TABLE handover_records (
    id                   BIGINT PRIMARY KEY AUTO_INCREMENT,
    record_number        VARCHAR(50)   UNIQUE NOT NULL,
    handover_date        DATE          NOT NULL,
    from_staff           VARCHAR(100),
    from_department      VARCHAR(100),
    to_staff             VARCHAR(100),
    to_department        VARCHAR(100),
    reason               TEXT,
    total_items          INT,
    created_at           TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE handover_items (
    id                   BIGINT PRIMARY KEY AUTO_INCREMENT,
    handover_id          BIGINT        NOT NULL,
    copy_id              BIGINT,
    document_code        VARCHAR(50),
    document_name        VARCHAR(500),
    quantity             INT           DEFAULT 1,
    condition_status     VARCHAR(50),
    notes                VARCHAR(200),

    FOREIGN KEY (handover_id) REFERENCES handover_records(id),
    FOREIGN KEY (copy_id)     REFERENCES physical_copies(id)
);

-- ─── Bảng sequence cho ĐKCB và mã vạch ───────────────────────
CREATE TABLE id_sequences (
    seq_name     VARCHAR(50) PRIMARY KEY,
    current_val  BIGINT      NOT NULL DEFAULT 1,
    description  VARCHAR(100)
);

INSERT INTO id_sequences VALUES
    ('BARCODE',  1000, 'Mã vạch (PKA-YYYYMMDD-NNNNNN)'),
    ('DKCB_GT',  1,    'ĐKCB Giáo trình (GT.YYYY.NNNNNN)'),
    ('DKCB_TK',  1,    'ĐKCB Tài liệu tham khảo'),
    ('DKCB_LV',  1,    'ĐKCB Luận văn'),
    ('DKCB_LA',  1,    'ĐKCB Luận án'),
    ('DKCB_BC',  1,    'ĐKCB Báo cáo khoa học'),
    ('DKCB_TC',  1,    'ĐKCB Tạp chí');

-- ─── View: Danh mục tra cứu tổng hợp ─────────────────────────
CREATE VIEW v_catalog AS
SELECT
    d.id,
    d.isbn,
    d.title,
    d.author,
    d.publisher,
    d.publish_year,
    d.ddc_code,
    d.ddc_class_name,
    d.call_number,
    d.document_type,
    d.warehouse_location,
    COUNT(c.id)                                            AS total_copies,
    SUM(CASE WHEN c.status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available_copies
FROM documents d
LEFT JOIN physical_copies c ON c.document_id = d.id
GROUP BY d.id;

-- ─── View: Sổ ĐKCB tổng hợp (lưu lâu dài) ───────────────────
CREATE VIEW v_accession_register AS
SELECT
    c.accession_number          AS dkcb,
    d.title,
    d.author,
    d.publisher,
    d.publish_year,
    d.document_type,
    d.warehouse_location        AS kho,
    c.shelf_location            AS gia_sach,
    c.barcode,
    c.status,
    c.received_date,
    c.processed_by              AS nv_bien_muc
FROM physical_copies c
JOIN documents d ON d.id = c.document_id
ORDER BY c.accession_number;