package com.tmc.attendance.kiosk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class QrCodeDecoderControllerTest {

    private final QrCodeDecoderController controller = new QrCodeDecoderController();

    @Test
    void decodesQrTokenFromJpegImage() throws Exception {
        var matrix = new MultiFormatWriter().encode("EMP:E0001", BarcodeFormat.QR_CODE, 320, 320);
        var output = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "jpg", output);

        var response = controller.decode(output.toByteArray());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("EMP:E0001", response.getBody().token());
    }

    @Test
    void returnsNoContentWhenImageHasNoQrCode() throws Exception {
        var image = new java.awt.image.BufferedImage(100, 100, java.awt.image.BufferedImage.TYPE_INT_RGB);
        var output = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", output);

        var response = controller.decode(output.toByteArray());

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void rejectsEmptyOrOversizedImage() throws Exception {
        assertEquals(HttpStatus.BAD_REQUEST, controller.decode(new byte[0]).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.decode(new byte[1_000_001]).getStatusCode());
    }
}
