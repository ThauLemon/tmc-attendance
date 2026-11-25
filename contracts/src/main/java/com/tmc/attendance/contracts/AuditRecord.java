package com.tmc.attendance.contracts;

import java.time.Instant;

/** Một dòng nhật ký kiểm toán. Chỉ được INSERT, không sửa, không xóa. */
public record AuditRecord(Instant occurredAt, String actor, String action, String target, String detail) {
}
