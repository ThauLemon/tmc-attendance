package com.tmc.attendance.kiosk;

import com.tmc.attendance.contracts.Direction;
import com.tmc.attendance.contracts.GateResult;
import com.tmc.attendance.contracts.IGateController;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

/**
 * Turnstile giả lập trong kiosk-agent (Hình 10 của M1). Thay được cho adapter phần cứng thật (LSP).
 * Đổi chế độ lúc chạy qua POST /api/simulator/mode để demo timeout.
 *
 */
@Component
public class TurnstileSimulator implements IGateController {

    private volatile SimulatorMode mode;
    private final ConcurrentMap<String, GateResult> transactionResults = new ConcurrentHashMap<>();

    public TurnstileSimulator(KioskProperties properties) {
        this.mode = properties.simulatorMode();
    }

    @Override
    public GateResult open(String transactionId, Direction direction) {
        return transactionResults.computeIfAbsent(transactionId, ignored -> switch (mode) {
            case OK -> {
                pause(50);
                yield GateResult.OPENED;
            }
            case TIMEOUT -> {
                pause(500);
                yield GateResult.TIMEOUT;
            }
            case FAULT -> GateResult.FAULT;
        });
    }

    @Override
    public GateResult getStatus(String transactionId) {
        return transactionResults.getOrDefault(transactionId, GateResult.TIMEOUT);
    }

    public SimulatorMode getMode() {
        return mode;
    }

    public void setMode(SimulatorMode mode) {
        this.mode = mode;
    }

    private static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
