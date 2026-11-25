package com.tmc.attendance.loadtest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SKELETON: gửi lần lượt N lượt quét tới một kiosk rồi in p95.
 * Cách chạy: {@code java -cp load-test/target/classes com.tmc.attendance.loadtest.LoadTestMain http://localhost:8082 50}
 *
 * <p>TODO Vũ: gửi song song 800 lượt (nén thời gian); khi có validator thật thì sinh token ngay lúc gửi
 * bằng QrTokenSigner (token hết hạn sau 15 giây, không tạo sẵn được).
 */
public final class LoadTestMain {

    private LoadTestMain() {
    }

    public static void main(String[] args) throws Exception {
        String kioskUrl = args.length > 0 ? args[0] : "http://localhost:8082";
        int total = args.length > 1 ? Integer.parseInt(args[1]) : 50;
        if (total <= 0) {
            throw new IllegalArgumentException("Số lượt phải lớn hơn 0");
        }

        HttpClient client = HttpClient.newHttpClient();
        List<Long> latencies = new ArrayList<>();
        int ok = 0;
        for (int i = 0; i < total; i++) {
            String body = "{\"token\":\"EMP:E%04d\",\"direction\":\"IN\"}".formatted(i);
            HttpRequest request = HttpRequest.newBuilder(URI.create(kioskUrl + "/api/scan"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            long start = System.nanoTime();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            latencies.add((System.nanoTime() - start) / 1_000_000);
            if (response.statusCode() == 200) {
                ok++;
            }
        }

        Collections.sort(latencies);
        long p95 = latencies.get((int) Math.ceil(0.95 * latencies.size()) - 1);
        System.out.printf("Gửi %d lượt, %d lượt HTTP 200, p95 = %d ms%n", total, ok, p95);
    }
}
