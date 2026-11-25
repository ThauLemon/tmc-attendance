package com.tmc.attendance.contracts;

import java.util.List;

/** Lưu trữ cục bộ tại kiosk (SQLite). Cài đặt: Nam. */
public interface IAttendanceStore {

    /** Ghi bản ghi PENDING trước khi mở cổng. Phải bền (đã commit) khi hàm trả về. */
    void saveBeforeOpen(AttendanceEvent event);

    void updateStatus(String transactionId, AttendanceStatus status);

    List<AttendanceEvent> findByStatus(AttendanceStatus status);

    /** Các bản ghi đã có kết quả cuối (không còn PENDING) nhưng chưa được broker xác nhận. */
    List<AttendanceEvent> findUnsynced(int limit);

    void markSynced(String transactionId);
}
