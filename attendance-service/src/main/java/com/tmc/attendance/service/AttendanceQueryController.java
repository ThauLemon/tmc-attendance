package com.tmc.attendance.service;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceQueryController {

    private final AttendanceRecordRepository repository;

    public AttendanceQueryController(AttendanceRecordRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Map<String, Object>> byEmployee(@RequestParam("employeeId") String employeeId) {
        return repository.findByEmployee(employeeId);
    }

    /** Tổng số bản ghi, dùng trong demo mất WAN: quét 50 lượt thì phải đủ 50, không trùng. */
    @GetMapping("/count")
    public Map<String, Long> count() {
        return Map.of("total", repository.count());
    }
}
