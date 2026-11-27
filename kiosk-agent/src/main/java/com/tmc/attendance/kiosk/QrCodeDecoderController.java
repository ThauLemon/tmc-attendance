package com.tmc.attendance.kiosk;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/qr")
public class QrCodeDecoderController {

    private static final int MAX_IMAGE_BYTES = 1_000_000;
    private static final Map<DecodeHintType, Object> DECODE_HINTS = Map.of(
            DecodeHintType.POSSIBLE_FORMATS, List.of(BarcodeFormat.QR_CODE),
            DecodeHintType.TRY_HARDER, Boolean.TRUE);

    @PostMapping(value = "/decode", consumes = MediaType.IMAGE_JPEG_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DecodedQr> decode(@RequestBody byte[] imageBytes) throws IOException {
        if (imageBytes.length == 0 || imageBytes.length > MAX_IMAGE_BYTES) {
            return ResponseEntity.badRequest().build();
        }

        var image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (image == null) {
            return ResponseEntity.badRequest().build();
        }

        var bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
        var reader = new MultiFormatReader();
        try {
            var result = reader.decode(bitmap, DECODE_HINTS);
            return ResponseEntity.ok(new DecodedQr(result.getText()));
        } catch (NotFoundException ignored) {
            return ResponseEntity.noContent().build();
        } finally {
            reader.reset();
        }
    }

    public record DecodedQr(String token) {
    }
}
