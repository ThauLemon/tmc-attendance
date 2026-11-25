package com.tmc.attendance.contracts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.ECGenParameterSpec;
import org.junit.jupiter.api.Test;

class QrTokenSignerTest {

    private static KeyPair newDeviceKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        return generator.generateKeyPair();
    }

    @Test
    void signedTokenVerifiesAfterEncodeAndParse() throws Exception {
        KeyPair keys = newDeviceKeyPair();
        QrToken token = QrTokenSigner.sign(keys.getPrivate(), "dev-001", 1_790_000_000L, QrTokenSigner.randomNonce());

        QrToken parsed = QrToken.parse(token.encode());

        assertEquals(token, parsed);
        assertTrue(QrTokenSigner.verify(keys.getPublic(), parsed));
    }

    @Test
    void tamperedTimestampFailsVerification() throws Exception {
        KeyPair keys = newDeviceKeyPair();
        QrToken token = QrTokenSigner.sign(keys.getPrivate(), "dev-001", 1_790_000_000L, "abc");
        QrToken tampered = new QrToken(token.version(), token.deviceId(), token.issuedAtEpochSeconds() + 60,
                token.nonce(), token.signatureBase64Url());

        assertFalse(QrTokenSigner.verify(keys.getPublic(), tampered));
    }

    @Test
    void tokenSignedByAnotherDeviceFailsVerification() throws Exception {
        QrToken token = QrTokenSigner.sign(newDeviceKeyPair().getPrivate(), "dev-001", 1_790_000_000L, "abc");

        assertFalse(QrTokenSigner.verify(newDeviceKeyPair().getPublic(), token));
    }

    @Test
    void malformedTokenIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> QrToken.parse("v1.dev-001.abc"));
        assertThrows(IllegalArgumentException.class, () -> QrToken.parse("v1.dev-001.not-a-number.n.sig"));
    }
}
