package com.phenikaa.library.service;

import com.phenikaa.library.model.Document;
import com.phenikaa.library.model.Document.DocumentType;
import com.phenikaa.library.model.Document.WarehouseLocation;

import java.util.*;

/**
 * CATALOGING SERVICE (Xử lý nghiệp vụ biên mục)
 *
 * Thực hiện Bước 3 của quy trình QT.02.TV.NV:
 *   - Phân loại theo DDC 23
 *   - Tra từ khoá (subject heading)
 *   - Tính chỉ số Cutter theo nhan đề
 *   - Tạo ký hiệu xếp giá (Call Number)
 *   - Biên mục vào phần mềm thư viện Koha
 *   - Xác định kho lưu trữ
 *
 * Thời gian chuẩn: 15 phút/tài liệu (theo bảng lưu đồ Phenikaa)
 */
public class CatalogingService {

    // ─── Bảng DDC 23 – Lớp chính ──────────────────────────────────────────────
    private static final Map<String, String> DDC_MAIN_CLASSES = new LinkedHashMap<>();
    static {
        DDC_MAIN_CLASSES.put("000", "Tin học, Thông tin & Tác phẩm tổng quát");
        DDC_MAIN_CLASSES.put("100", "Triết học & Tâm lý học");
        DDC_MAIN_CLASSES.put("200", "Tôn giáo");
        DDC_MAIN_CLASSES.put("300", "Khoa học xã hội");
        DDC_MAIN_CLASSES.put("400", "Ngôn ngữ");
        DDC_MAIN_CLASSES.put("500", "Khoa học tự nhiên & Toán học");
        DDC_MAIN_CLASSES.put("600", "Kỹ thuật & Công nghệ");
        DDC_MAIN_CLASSES.put("700", "Nghệ thuật & Giải trí");
        DDC_MAIN_CLASSES.put("800", "Văn học");
        DDC_MAIN_CLASSES.put("900", "Lịch sử & Địa lý");
    }

    // ─── Bảng DDC 23 – Phân mục mở rộng ──────────────────────────────────────
    private static final Map<String, String> DDC_SECTIONS = new LinkedHashMap<>();
    static {
        // 000 – Tin học
        DDC_SECTIONS.put("001",   "Tri thức");
        DDC_SECTIONS.put("004",   "Xử lý & Khoa học máy tính");
        DDC_SECTIONS.put("005",   "Lập trình máy tính & Chương trình");
        DDC_SECTIONS.put("005.1", "Lập trình");
        DDC_SECTIONS.put("005.13","Ngôn ngữ lập trình");
        DDC_SECTIONS.put("005.133","Các ngôn ngữ lập trình cụ thể");
        DDC_SECTIONS.put("005.74","Quản trị cơ sở dữ liệu");
        DDC_SECTIONS.put("005.8", "An toàn thông tin");
        DDC_SECTIONS.put("006",   "Phương pháp máy tính đặc biệt");
        DDC_SECTIONS.put("006.3", "Trí tuệ nhân tạo");
        DDC_SECTIONS.put("006.31","Học máy");
        DDC_SECTIONS.put("006.32","Mạng nơ-ron nhân tạo");
        // 100 – Triết học
        DDC_SECTIONS.put("150",   "Tâm lý học");
        DDC_SECTIONS.put("152",   "Tâm lý nhận thức và hành vi");
        DDC_SECTIONS.put("158",   "Tâm lý ứng dụng");
        // 300 – Khoa học xã hội
        DDC_SECTIONS.put("302",   "Tương tác xã hội");
        DDC_SECTIONS.put("330",   "Kinh tế học");
        DDC_SECTIONS.put("332",   "Kinh tế tài chính");
        DDC_SECTIONS.put("336",   "Tài chính công");
        DDC_SECTIONS.put("338",   "Sản xuất");
        DDC_SECTIONS.put("340",   "Luật pháp");
        DDC_SECTIONS.put("346",   "Luật tư");
        DDC_SECTIONS.put("347",   "Tố tụng dân sự");
        DDC_SECTIONS.put("350",   "Hành chính công");
        DDC_SECTIONS.put("370",   "Giáo dục");
        DDC_SECTIONS.put("378",   "Giáo dục đại học");
        DDC_SECTIONS.put("380",   "Thương mại & Truyền thông");
        // 500 – Khoa học tự nhiên
        DDC_SECTIONS.put("510",   "Toán học");
        DDC_SECTIONS.put("511",   "Toán học tổng quát");
        DDC_SECTIONS.put("512",   "Đại số");
        DDC_SECTIONS.put("515",   "Giải tích");
        DDC_SECTIONS.put("519",   "Xác suất thống kê");
        DDC_SECTIONS.put("530",   "Vật lý");
        DDC_SECTIONS.put("531",   "Cơ học cổ điển");
        DDC_SECTIONS.put("540",   "Hóa học");
        DDC_SECTIONS.put("541",   "Hóa học lý thuyết");
        DDC_SECTIONS.put("546",   "Hóa vô cơ");
        DDC_SECTIONS.put("547",   "Hóa hữu cơ");
        DDC_SECTIONS.put("570",   "Sinh học");
        DDC_SECTIONS.put("580",   "Thực vật học");
        DDC_SECTIONS.put("590",   "Động vật học");
        // 600 – Kỹ thuật
        DDC_SECTIONS.put("610",   "Y học & Sức khỏe");
        DDC_SECTIONS.put("615",   "Dược học");
        DDC_SECTIONS.put("620",   "Kỹ thuật & Kỹ thuật liên quan");
        DDC_SECTIONS.put("621",   "Kỹ thuật vật lý ứng dụng");
        DDC_SECTIONS.put("621.3", "Kỹ thuật điện, điện tử");
        DDC_SECTIONS.put("623",   "Kỹ thuật quân sự");
        DDC_SECTIONS.put("624",   "Kỹ thuật xây dựng");
        DDC_SECTIONS.put("625",   "Kỹ thuật đường sắt và đường bộ");
        DDC_SECTIONS.put("628",   "Kỹ thuật môi trường");
        DDC_SECTIONS.put("630",   "Nông nghiệp & kỹ thuật liên quan");
        DDC_SECTIONS.put("650",   "Quản lý & Dịch vụ phụ trợ");
        DDC_SECTIONS.put("657",   "Kế toán");
        DDC_SECTIONS.put("658",   "Quản lý tổng hợp");
        DDC_SECTIONS.put("658.3", "Quản lý nhân sự");
        DDC_SECTIONS.put("658.4", "Quản trị cấp cao");
        DDC_SECTIONS.put("658.8", "Quản lý marketing");
        DDC_SECTIONS.put("660",   "Kỹ thuật hóa học");
        DDC_SECTIONS.put("670",   "Công nghệ chế tạo");
        // 800 – Văn học
        DDC_SECTIONS.put("895.9", "Văn học Việt Nam");
        DDC_SECTIONS.put("895.92","Tiểu thuyết Việt Nam");
        DDC_SECTIONS.put("895.93","Truyện ngắn Việt Nam");
        // 900 – Lịch sử
        DDC_SECTIONS.put("959",   "Lịch sử Đông Nam Á");
        DDC_SECTIONS.put("959.7", "Lịch sử Việt Nam");
    }

    // ─── Từ điển từ khoá → DDC ────────────────────────────────────────────────
    private static final List<String[]> KEYWORD_TO_DDC = new ArrayList<>();
    static {
        // {từ khoá regex, mã DDC, tên lớp}
        KEYWORD_TO_DDC.add(new String[]{"java|spring|hibernate|maven|gradle", "005.133", "Ngôn ngữ lập trình Java"});
        KEYWORD_TO_DDC.add(new String[]{"python|django|flask|pandas|numpy", "005.133", "Ngôn ngữ lập trình Python"});
        KEYWORD_TO_DDC.add(new String[]{"javascript|nodejs|react|vue|angular", "005.133", "Ngôn ngữ lập trình JavaScript"});
        KEYWORD_TO_DDC.add(new String[]{"c\\+\\+|c#|golang|rust|kotlin|swift", "005.133", "Ngôn ngữ lập trình"});
        KEYWORD_TO_DDC.add(new String[]{"lập trình|programming|algorithm|thuật toán|cấu trúc dữ liệu|data structure", "005.1", "Lập trình"});
        KEYWORD_TO_DDC.add(new String[]{"cơ sở dữ liệu|database|sql|mysql|postgresql|oracle|mongodb", "005.74", "Quản trị cơ sở dữ liệu"});
        KEYWORD_TO_DDC.add(new String[]{"mạng máy tính|network|tcp|ip|cisco|router|switch|wifi", "004.6", "Mạng máy tính"});
        KEYWORD_TO_DDC.add(new String[]{"bảo mật|security|mã hóa|cryptograph|hack|penetration", "005.8", "Bảo mật thông tin"});
        KEYWORD_TO_DDC.add(new String[]{"trí tuệ nhân tạo|artificial intelligence|ai|machine learning|học máy", "006.3", "Trí tuệ nhân tạo"});
        KEYWORD_TO_DDC.add(new String[]{"deep learning|neural network|mạng nơ-ron|pytorch|tensorflow|keras", "006.32", "Học sâu"});
        KEYWORD_TO_DDC.add(new String[]{"toán|mathematics|đại số|algebra|giải tích|calculus", "510", "Toán học"});
        KEYWORD_TO_DDC.add(new String[]{"xác suất|thống kê|statistics|probability", "519", "Xác suất thống kê"});
        KEYWORD_TO_DDC.add(new String[]{"vật lý|physics|cơ học|nhiệt học|điện từ", "530", "Vật lý"});
        KEYWORD_TO_DDC.add(new String[]{"hóa học|chemistry|phản ứng|hóa hữu cơ|hóa vô cơ", "540", "Hóa học"});
        KEYWORD_TO_DDC.add(new String[]{"sinh học|biology|di truyền|tế bào|vi sinh|gene", "570", "Sinh học"});
        KEYWORD_TO_DDC.add(new String[]{"kỹ thuật điện|điện tử|electronic|circuit|mạch điện", "621.3", "Kỹ thuật điện - điện tử"});
        KEYWORD_TO_DDC.add(new String[]{"xây dựng|civil|kết cấu|bê tông|nền móng", "624", "Kỹ thuật xây dựng"});
        KEYWORD_TO_DDC.add(new String[]{"cơ khí|mechanical|động cơ|turbine|nhiệt|chế tạo máy", "621", "Kỹ thuật cơ khí"});
        KEYWORD_TO_DDC.add(new String[]{"y học|medicine|dược|điều dưỡng|giải phẫu|bệnh lý", "610", "Y học"});
        KEYWORD_TO_DDC.add(new String[]{"dược|pharmacology|thuốc|bào chế", "615", "Dược học"});
        KEYWORD_TO_DDC.add(new String[]{"kế toán|accounting|kiểm toán|audit|tài khoản", "657", "Kế toán"});
        KEYWORD_TO_DDC.add(new String[]{"quản trị kinh doanh|mba|management|quản lý doanh nghiệp", "658", "Quản lý tổng hợp"});
        KEYWORD_TO_DDC.add(new String[]{"marketing|thị trường|thương hiệu|brand|quảng cáo", "658.8", "Quản lý marketing"});
        KEYWORD_TO_DDC.add(new String[]{"nhân sự|human resource|hr|tuyển dụng|đào tạo nhân viên", "658.3", "Quản lý nhân sự"});
        KEYWORD_TO_DDC.add(new String[]{"kinh tế|economics|vi mô|vĩ mô|microeconomics|macroeconomics", "330", "Kinh tế học"});
        KEYWORD_TO_DDC.add(new String[]{"tài chính|finance|ngân hàng|banking|chứng khoán|investment", "332", "Kinh tế tài chính"});
        KEYWORD_TO_DDC.add(new String[]{"luật|law|pháp luật|hiến pháp|dân sự|hình sự|tố tụng", "340", "Luật pháp"});
        KEYWORD_TO_DDC.add(new String[]{"giáo dục|education|sư phạm|pedagogy|dạy học|curriculum", "370", "Giáo dục"});
        KEYWORD_TO_DDC.add(new String[]{"tâm lý|psychology|hành vi|nhận thức|cảm xúc", "150", "Tâm lý học"});
        KEYWORD_TO_DDC.add(new String[]{"nông nghiệp|agriculture|trồng trọt|chăn nuôi|thủy sản", "630", "Nông nghiệp"});
        KEYWORD_TO_DDC.add(new String[]{"môi trường|environment|ô nhiễm|xử lý nước|khí thải", "628", "Kỹ thuật môi trường"});
        KEYWORD_TO_DDC.add(new String[]{"văn học|tiểu thuyết|truyện ngắn|thơ|kịch|fiction", "895.9", "Văn học Việt Nam"});
        KEYWORD_TO_DDC.add(new String[]{"lịch sử việt nam|việt nam|vietnam history|kháng chiến|triều đại", "959.7", "Lịch sử Việt Nam"});
        KEYWORD_TO_DDC.add(new String[]{"lịch sử|history|cổ đại|trung đại|thế giới|chiến tranh", "900", "Lịch sử"});
    }

    // ─── Bảng Cutter (Chỉ số Cutter theo nhan đề) ────────────────────────────
    // Cutter-Sanborn Three-Figure Table (simplified for Vietnamese)
    private static final TreeMap<String, String> CUTTER_TABLE = new TreeMap<>();
    static {
        CUTTER_TABLE.put("A",   "A1");   CUTTER_TABLE.put("An",  "A53");
        CUTTER_TABLE.put("B",   "B1");   CUTTER_TABLE.put("Ba",  "B12");
        CUTTER_TABLE.put("Bi",  "B55");  CUTTER_TABLE.put("Bo",  "B63");
        CUTTER_TABLE.put("Bu",  "B86");  CUTTER_TABLE.put("C",   "C1");
        CUTTER_TABLE.put("Ca",  "C12");  CUTTER_TABLE.put("Ch",  "C45");
        CUTTER_TABLE.put("Co",  "C63");  CUTTER_TABLE.put("Cu",  "C86");
        CUTTER_TABLE.put("D",   "D1");   CUTTER_TABLE.put("Da",  "D12");
        CUTTER_TABLE.put("De",  "D35");  CUTTER_TABLE.put("Di",  "D55");
        CUTTER_TABLE.put("Do",  "D63");  CUTTER_TABLE.put("Du",  "D86");
        CUTTER_TABLE.put("E",   "E1");   CUTTER_TABLE.put("F",   "F1");
        CUTTER_TABLE.put("G",   "G1");   CUTTER_TABLE.put("Gi",  "G55");
        CUTTER_TABLE.put("H",   "H1");   CUTTER_TABLE.put("Ha",  "H12");
        CUTTER_TABLE.put("He",  "H35");  CUTTER_TABLE.put("Hi",  "H55");
        CUTTER_TABLE.put("Ho",  "H63");  CUTTER_TABLE.put("Hu",  "H86");
        CUTTER_TABLE.put("I",   "I1");   CUTTER_TABLE.put("J",   "J1");
        CUTTER_TABLE.put("K",   "K1");   CUTTER_TABLE.put("Kh",  "K45");
        CUTTER_TABLE.put("Ki",  "K55");  CUTTER_TABLE.put("Ko",  "K63");
        CUTTER_TABLE.put("L",   "L1");   CUTTER_TABLE.put("La",  "L12");
        CUTTER_TABLE.put("Le",  "L35");  CUTTER_TABLE.put("Li",  "L55");
        CUTTER_TABLE.put("Lo",  "L63");  CUTTER_TABLE.put("Lu",  "L86");
        CUTTER_TABLE.put("M",   "M1");   CUTTER_TABLE.put("Ma",  "M12");
        CUTTER_TABLE.put("Me",  "M35");  CUTTER_TABLE.put("Mi",  "M55");
        CUTTER_TABLE.put("Mo",  "M63");  CUTTER_TABLE.put("Mu",  "M86");
        CUTTER_TABLE.put("N",   "N1");   CUTTER_TABLE.put("Na",  "N12");
        CUTTER_TABLE.put("Ne",  "N35");  CUTTER_TABLE.put("Ng",  "N48");
        CUTTER_TABLE.put("Ngu", "N489"); CUTTER_TABLE.put("Ni",  "N55");
        CUTTER_TABLE.put("No",  "N63");  CUTTER_TABLE.put("Nu",  "N86");
        CUTTER_TABLE.put("O",   "O1");   CUTTER_TABLE.put("P",   "P1");
        CUTTER_TABLE.put("Ph",  "P45");  CUTTER_TABLE.put("Pi",  "P55");
        CUTTER_TABLE.put("Pr",  "P75");  CUTTER_TABLE.put("Q",   "Q1");
        CUTTER_TABLE.put("Qu",  "Q83");  CUTTER_TABLE.put("R",   "R1");
        CUTTER_TABLE.put("S",   "S1");   CUTTER_TABLE.put("Sa",  "S12");
        CUTTER_TABLE.put("Se",  "S35");  CUTTER_TABLE.put("Si",  "S55");
        CUTTER_TABLE.put("So",  "S63");  CUTTER_TABLE.put("Su",  "S86");
        CUTTER_TABLE.put("T",   "T1");   CUTTER_TABLE.put("Ta",  "T12");
        CUTTER_TABLE.put("Te",  "T35");  CUTTER_TABLE.put("Th",  "T45");
        CUTTER_TABLE.put("Ti",  "T55");  CUTTER_TABLE.put("To",  "T63");
        CUTTER_TABLE.put("Tr",  "T75");  CUTTER_TABLE.put("Tu",  "T86");
        CUTTER_TABLE.put("U",   "U1");   CUTTER_TABLE.put("V",   "V1");
        CUTTER_TABLE.put("Va",  "V12");  CUTTER_TABLE.put("Vi",  "V55");
        CUTTER_TABLE.put("Vo",  "V63");  CUTTER_TABLE.put("Vu",  "V86");
        CUTTER_TABLE.put("W",   "W1");   CUTTER_TABLE.put("X",   "X1");
        CUTTER_TABLE.put("Y",   "Y1");   CUTTER_TABLE.put("Z",   "Z1");
    }

    // ─── API công khai ────────────────────────────────────────────────────────

    /**
     * Bước 3: Thực hiện toàn bộ xử lý nghiệp vụ biên mục cho một tài liệu.
     * Bao gồm: phân loại DDC, tra từ khoá, tính Cutter, tạo Call Number, xác định kho.
     *
     * @param doc Tài liệu cần biên mục
     * @param staffName Tên NV nghiệp vụ thực hiện
     */
    public void performCataloging(Document doc, String staffName) {
        System.out.println("\n── BƯỚC 3: XỬ LÝ NGHIỆP VỤ BIÊN MỤC ──────────────────");
        System.out.println("Tài liệu: " + doc.getTitle());
        System.out.println("NV thực hiện: " + staffName);

        // 3a. Phân loại DDC
        String[] ddcResult = suggestDdc(doc.getTitle(), doc.getSubjectHeading(), doc.getKeywords());
        if (doc.getDdcCode() == null || doc.getDdcCode().isBlank()) {
            doc.setDdcCode(ddcResult[0]);
            doc.setDdcClassName(ddcResult[1]);
            System.out.printf("  → DDC: %s – %s%n", ddcResult[0], ddcResult[1]);
        } else {
            doc.setDdcClassName(getDdcClassName(doc.getDdcCode()));
            System.out.printf("  → DDC (thủ công): %s – %s%n", doc.getDdcCode(), doc.getDdcClassName());
        }

        // 3b. Tính chỉ số Cutter theo nhan đề
        String cutter = computeCutterNumber(doc.getTitle());
        doc.setCutterNumber(cutter);
        System.out.printf("  → Cutter: %s%n", cutter);

        // 3c. Tạo Call Number = DDC + Cutter + Năm
        String callNumber = buildCallNumber(doc.getDdcCode(), cutter, doc.getPublishYear());
        doc.setCallNumber(callNumber);
        System.out.printf("  → Ký hiệu xếp giá: %s%n", callNumber);

        // 3d. Xác định kho lưu trữ theo loại tài liệu
        WarehouseLocation location = determineWarehouse(doc.getDocumentType(), doc.getDdcCode());
        doc.setWarehouseLocation(location);
        System.out.printf("  → Kho: %s (%s)%n", location.getLabel(), location.getCode());

        // 3e. Ghi nhận thông tin biên mục
        doc.setCatalogedDate(java.time.LocalDate.now());
        doc.setCatalogedBy(staffName);

        System.out.println("  ✓ Biên mục vào phần mềm TV Koha – HOÀN THÀNH");
    }

    /**
     * Gợi ý mã DDC dựa trên nhan đề, đề mục chủ đề, từ khoá
     *
     * @return String[2] = {mã DDC, tên lớp DDC}
     */
    public String[] suggestDdc(String title, String subjectHeading, String keywords) {
        String combined = "";
        if (title          != null) combined += title.toLowerCase() + " ";
        if (subjectHeading != null) combined += subjectHeading.toLowerCase() + " ";
        if (keywords       != null) combined += keywords.toLowerCase();

        for (String[] entry : KEYWORD_TO_DDC) {
            if (combined.matches(".*(" + entry[0] + ").*")) {
                return new String[]{entry[1], entry[2]};
            }
        }
        return new String[]{"000", "Chưa phân loại"};
    }

    /**
     * Tính chỉ số Cutter theo nhan đề (Cutter-Sanborn, adapted for Vietnamese)
     *
     * Quy tắc:
     *   1. Bỏ mạo từ đầu: "The ", "A ", "An ", "Một ", "Những ", "Các "
     *   2. Lấy 3 ký tự đầu của từ đầu tiên có nghĩa
     *   3. Tra bảng Cutter để lấy số
     */
    public String computeCutterNumber(String title) {
        if (title == null || title.isBlank()) return "Z99";

        // Chuẩn hóa: bỏ mạo từ
        String normalized = title.trim();
        String[] articles = {"The ", "A ", "An ", "Một ", "Những ", "Các ", "Một số "};
        for (String art : articles) {
            if (normalized.startsWith(art)) {
                normalized = normalized.substring(art.length()).trim();
                break;
            }
        }

        // Lấy từ đầu tiên, bỏ dấu tiếng Việt
        String firstWord = normalized.split("\\s+")[0];
        firstWord = removeDiacritics(firstWord);

        // Thử khớp từ dài nhất trước
        for (int len = Math.min(4, firstWord.length()); len >= 1; len--) {
            String prefix = capitalize(firstWord.substring(0, len));
            if (CUTTER_TABLE.containsKey(prefix)) {
                return CUTTER_TABLE.get(prefix);
            }
        }

        // Không khớp: dùng 2 ký tự đầu viết hoa
        return firstWord.substring(0, Math.min(2, firstWord.length())).toUpperCase() + "1";
    }

    /**
     * Xây dựng ký hiệu xếp giá (Call Number)
     * Format chuẩn Phenikaa: DDC Cutter Năm
     * Ví dụ: 005.133 N489 2023
     */
    public String buildCallNumber(String ddcCode, String cutterNumber, int year) {
        return String.format("%s %s %d", ddcCode, cutterNumber, year);
    }

    /**
     * Xác định kho lưu trữ phù hợp theo quy định của Phenikaa:
     *   - Phòng đọc mở: sách tham khảo, sách phổ thông
     *   - Kho giáo trình: giáo trình giảng dạy
     *   - Phòng bảo quản: xếp theo ĐKCB
     *   - Kho luận văn: luận văn, luận án, báo cáo KH
     */
    public WarehouseLocation determineWarehouse(DocumentType type, String ddcCode) {
        if (type == null) return WarehouseLocation.PHONG_DOC_MO;

        switch (type) {
            case GIAO_TRINH:
                return WarehouseLocation.KHO_GIAO_TRINH;
            case LUAN_VAN:
            case LUAN_AN:
            case BAO_CAO_KHOA_HOC:
                return WarehouseLocation.KHO_LUAN_VAN;
            case TAI_LIEU_THAM_KHAO:
            case SACH_THAM_KHAO:
                // Tài liệu xếp theo môn loại DDC vào phòng đọc mở
                return WarehouseLocation.PHONG_DOC_MO;
            default:
                return WarehouseLocation.PHONG_BAO_QUAN;
        }
    }

    /** Lấy tên lớp DDC từ mã */
    public String getDdcClassName(String ddcCode) {
        if (ddcCode == null) return "Chưa phân loại";
        // Tìm khớp dài nhất trước
        for (int len = ddcCode.length(); len >= 3; len--) {
            String key = ddcCode.substring(0, len);
            if (DDC_SECTIONS.containsKey(key))  return DDC_SECTIONS.get(key);
        }
        if (ddcCode.length() >= 1) {
            String mainKey = ddcCode.charAt(0) + "00";
            if (DDC_MAIN_CLASSES.containsKey(mainKey)) return DDC_MAIN_CLASSES.get(mainKey);
        }
        return "Không xác định";
    }

    /** Trả về toàn bộ bảng DDC chính để hiển thị UI */
    public Map<String, String> getAllMainClasses()  { return Collections.unmodifiableMap(DDC_MAIN_CLASSES); }
    public Map<String, String> getAllDdcSections()  { return Collections.unmodifiableMap(DDC_SECTIONS); }

    // ─── Tiện ích nội bộ ──────────────────────────────────────────────────────

    /** Bỏ dấu tiếng Việt (NFD + remove combining marks) */
    private String removeDiacritics(String text) {
        String normalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                         .replaceAll("[đĐ]", "d");
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}