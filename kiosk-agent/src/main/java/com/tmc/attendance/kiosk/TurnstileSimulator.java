package com.tmc.attendance.kiosk;

import com.tmc.attendance.contracts.Direction;
import com.tmc.attendance.contracts.GateResult;
import com.tmc.attendance.contracts.IGateController;
import org.springframework.stereotype.Component;

/**
 * Turnstile giả lập trong kiosk-agent (Hình 10 của M1). Thay được cho adapter phần cứng thật (LSP).
 * Đổi chế độ lúc chạy qua POST /api/simulator/mode để demo timeout.
 *
 * <p>TODO Nam: tra trạng thái theo transactionId cho quy trình đối soát (ADR-10).
 */
@Component
public class TurnstileSimulator implements IGateController {

    private volatile SimulatorMode mode;

    public TurnstileSimulator(KioskProperties properties) {
        this.mode = properties.simulatorMode();
    }

    @Override
    public GateResult open(String transactionId, Direction direction) {
        return switch (mode) {
            case OK -> {
                pause(50);
                yield GateResult.OPENED;
            }
            case TIMEOUT -> {
                pause(500);
                yield GateResult.TIMEOUT;
            }
            case FAULT -> GateResult.FAULT;
        };
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
