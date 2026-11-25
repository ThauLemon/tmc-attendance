package com.tmc.attendance.identity;

import com.tmc.attendance.contracts.DeviceKey;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Kiosk gọi API này để lấy danh sách khóa thiết bị hợp lệ (ADR-08).
 *
 * <p>SKELETON: đang trả danh sách rỗng. TODO Phú: lưu nhân viên và thiết bị đã đăng ký,
 * API đăng ký thiết bị, thu hồi thiết bị, phần đăng nhập của employee-qr-web.
 */
@RestController
@RequestMapping("/api/device-keys")
public class DeviceKeyController {

    @GetMapping
    public List<DeviceKey> activeDeviceKeys() {
        return List.of();
    }
}
