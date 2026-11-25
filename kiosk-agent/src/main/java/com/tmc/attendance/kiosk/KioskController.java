package com.tmc.attendance.kiosk;

import com.tmc.attendance.contracts.Direction;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class KioskController {

    public record ScanRequest(String token, Direction direction) {
    }

    private final AccessService accessService;
    private final TurnstileSimulator simulator;

    public KioskController(AccessService accessService, TurnstileSimulator simulator) {
        this.accessService = accessService;
        this.simulator = simulator;
    }

    /** Màn hình kiosk (webcam hoặc ô dán token) gọi API này cho mỗi lượt quét. */
    @PostMapping("/scan")
    public ScanResult scan(@RequestBody ScanRequest request) {
        Direction direction = request.direction() == null ? Direction.IN : request.direction();
        return accessService.scan(request.token(), direction);
    }

    @GetMapping("/simulator/mode")
    public SimulatorMode simulatorMode() {
        return simulator.getMode();
    }

    /** Đổi phản hồi của turnstile giả lập, ví dụ POST /api/simulator/mode?value=TIMEOUT. */
    @PostMapping("/simulator/mode")
    public SimulatorMode changeSimulatorMode(@RequestParam("value") SimulatorMode value) {
        simulator.setMode(value);
        return simulator.getMode();
    }
}
