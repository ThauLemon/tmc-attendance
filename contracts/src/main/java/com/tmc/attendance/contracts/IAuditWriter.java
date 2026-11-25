package com.tmc.attendance.contracts;

/** Ghi nhật ký kiểm toán. Cài đặt: Anh (audit-lib). */
public interface IAuditWriter {

    void write(AuditRecord record);
}
