package com.tmc.attendance.kiosk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tmc.attendance.contracts.AttendanceEvent;
import com.tmc.attendance.contracts.AttendanceStatus;
import com.tmc.attendance.contracts.Direction;
import com.tmc.attendance.contracts.GateResult;
import com.tmc.attendance.contracts.IAttendanceStore;
import com.tmc.attendance.contracts.IGateController;
import com.tmc.attendance.contracts.IQrValidator;
import com.tmc.attendance.contracts.ValidationResult;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Test AccessService bằng các cài đặt giả của interface trong contracts/, không cần Spring hay SQLite. */
class AccessServiceTest {

    private static final KioskProperties PROPS =
            new KioskProperties("kiosk-test", "site-test", SimulatorMode.OK, Duration.ofSeconds(1));
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-25T08:00:00Z"), ZoneOffset.UTC);

    private final InMemoryStore store = new InMemoryStore();
    private final IQrValidator acceptAll = (token, direction, now) -> ValidationResult.accept("E001", "dev-1", "n-1");

    @Test
    void writesPendingBeforeOpeningGateThenConfirms() {
        List<AttendanceStatus> statusSeenByGate = new ArrayList<>();
        IGateController gate = (txId, direction) -> {
            statusSeenByGate.add(store.events.get(txId).status());
            return GateResult.OPENED;
        };
        AccessService service = new AccessService(acceptAll, gate, store, PROPS, CLOCK);

        ScanResult result = service.scan("any", Direction.IN);

        assertEquals(List.of(AttendanceStatus.PENDING), statusSeenByGate);
        assertTrue(result.gateOpened());
        assertEquals(AttendanceStatus.CONFIRMED, result.status());
        assertEquals(AttendanceStatus.CONFIRMED, store.events.get(result.transactionId()).status());
    }

    @Test
    void gateTimeoutMarksPendingVerification() {
        AccessService service = new AccessService(acceptAll, (txId, d) -> GateResult.TIMEOUT, store, PROPS, CLOCK);

        ScanResult result = service.scan("any", Direction.IN);

        assertFalse(result.gateOpened());
        assertEquals(AttendanceStatus.PENDING_VERIFICATION, store.events.get(result.transactionId()).status());
    }

    @Test
    void invalidTokenIsRejectedWithoutStoringOrOpening() {
        IQrValidator rejectAll = (token, direction, now) -> ValidationResult.reject("hết hạn");
        IGateController gate = (txId, d) -> {
            throw new AssertionError("Không được mở cổng khi token sai");
        };
        AccessService service = new AccessService(rejectAll, gate, store, PROPS, CLOCK);

        ScanResult result = service.scan("bad", Direction.IN);

        assertNull(result.transactionId());
        assertEquals("hết hạn", result.reason());
        assertTrue(store.events.isEmpty());
    }

    private static final class InMemoryStore implements IAttendanceStore {
        final Map<String, AttendanceEvent> events = new LinkedHashMap<>();

        @Override
        public void saveBeforeOpen(AttendanceEvent event) {
            events.put(event.transactionId(), event);
        }

        @Override
        public void updateStatus(String transactionId, AttendanceStatus status) {
            events.computeIfPresent(transactionId, (id, e) -> e.withStatus(status));
        }

        @Override
        public List<AttendanceEvent> findByStatus(AttendanceStatus status) {
            return events.values().stream().filter(e -> e.status() == status).toList();
        }

        @Override
        public List<AttendanceEvent> findUnsynced(int limit) {
            return List.of();
        }

        @Override
        public void markSynced(String transactionId) {
        }
    }
}
