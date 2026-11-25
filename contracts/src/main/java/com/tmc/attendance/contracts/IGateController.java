package com.tmc.attendance.contracts;

/**
 * Điều khiển turnstile qua LAN. Cài đặt: TurnstileSimulator (kiosk-agent), sau này là adapter phần cứng.
 * Mọi cài đặt phải thay thế được cho nhau (LSP).
 */
public interface IGateController {

    GateResult open(String transactionId, Direction direction);
}
