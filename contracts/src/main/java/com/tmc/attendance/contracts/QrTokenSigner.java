package com.tmc.attendance.contracts;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Base64;

/**
 * Ký và kiểm chữ ký QR token phía Java. Dùng cho unit test, load-test và thư viện verify trong kiosk.
 * Phía điện thoại ký bằng WebCrypto với cùng thuật toán. BẢN NHÁP: Hưng phụ trách.
 */
public final class QrTokenSigner {

    /** WebCrypto xuất chữ ký dạng r||s (P1363), không phải DER, nên phải dùng biến thể này. */
    public static final String SIGNATURE_ALGORITHM = "SHA256withECDSAinP1363Format";

    private static final SecureRandom RANDOM = new SecureRandom();

    private QrTokenSigner() {
    }

    public static QrToken sign(PrivateKey deviceKey, String deviceId, long issuedAtEpochSeconds, String nonce) {
        QrToken unsigned = new QrToken(QrToken.CURRENT_VERSION, deviceId, issuedAtEpochSeconds, nonce, "");
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(deviceKey);
            signature.update(unsigned.signedPayload().getBytes(StandardCharsets.UTF_8));
            String sig = Base64.getUrlEncoder().withoutPadding().encodeToString(signature.sign());
            return new QrToken(unsigned.version(), deviceId, issuedAtEpochSeconds, nonce, sig);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Không ký được QR token", e);
        }
    }

    public static boolean verify(PublicKey deviceKey, QrToken token) {
        try {
            byte[] sig = Base64.getUrlDecoder().decode(token.signatureBase64Url());
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(deviceKey);
            signature.update(token.signedPayload().getBytes(StandardCharsets.UTF_8));
            return signature.verify(sig);
        } catch (IllegalArgumentException | GeneralSecurityException e) {
            return false;
        }
    }

    public static String randomNonce() {
        byte[] bytes = new byte[12];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
