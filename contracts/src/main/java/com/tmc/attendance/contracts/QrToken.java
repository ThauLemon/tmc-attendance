package com.tmc.attendance.contracts;

/**
 * Định dạng QR token (ADR-08). BẢN NHÁP: Châu và Hưng chốt trong contracts/.
 *
 * <pre>
 * v1.&lt;deviceId&gt;.&lt;issuedAtEpochSeconds&gt;.&lt;nonce&gt;.&lt;signatureBase64Url&gt;
 * </pre>
 *
 * <ul>
 *   <li>Chữ ký: ECDSA P-256, SHA-256, dạng P1363 (r||s, 64 byte), đúng dạng WebCrypto xuất ra.</li>
 *   <li>Chuỗi được ký: {@link #signedPayload()} dạng UTF-8, tức mọi thứ trước dấu chấm cuối cùng.
 *       Kiosk verify trên đúng chuỗi byte này, không parse rồi serialize lại.</li>
 *   <li>deviceId và nonce không được chứa dấu chấm; nonce và chữ ký dùng Base64URL không padding.</li>
 * </ul>
 */
public record QrToken(
        String version,
        String deviceId,
        long issuedAtEpochSeconds,
        String nonce,
        String signatureBase64Url) {

    public static final String CURRENT_VERSION = "v1";

    /** Chuỗi được ký (không gồm chữ ký). */
    public String signedPayload() {
        return String.join(".", version, deviceId, Long.toString(issuedAtEpochSeconds), nonce);
    }

    /** Chuỗi đầy đủ để hiển thị trong mã QR. */
    public String encode() {
        return signedPayload() + "." + signatureBase64Url;
    }

    public static QrToken parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("QR token rỗng");
        }
        String[] parts = raw.trim().split("\\.", -1);
        if (parts.length != 5) {
            throw new IllegalArgumentException("QR token phải có 5 phần, nhận được " + parts.length);
        }
        long issuedAt;
        try {
            issuedAt = Long.parseLong(parts[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Timestamp trong QR token không hợp lệ", e);
        }
        return new QrToken(parts[0], parts[1], issuedAt, parts[3], parts[4]);
    }
}
