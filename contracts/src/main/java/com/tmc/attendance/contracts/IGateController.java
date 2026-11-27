package com.tmc.attendance.contracts;

/**
 * Điều khiển turnstile qua LAN. Cài đặt: TurnstileSimulator (kiosk-agent), sau này là adapter phần cứng.
 * Mọi cài đặt phải thay thế được cho nhau (LSP).
 */
public interface IGateController {

    GateResult open(String transactionId, Direction direction);

    /**
     * Tra cứu kết quả đã biết của một giao dịch. TIMEOUT biểu thị chưa thể xác nhận kết quả.
     */
    default GateResult getStatus(String transactionId) {
        return GateResult.TIMEOUT;
    }
}
