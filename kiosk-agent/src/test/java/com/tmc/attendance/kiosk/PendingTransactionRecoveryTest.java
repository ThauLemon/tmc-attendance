package com.tmc.attendance.kiosk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tmc.attendance.contracts.AttendanceEvent;
import com.tmc.attendance.contracts.AttendanceStatus;
import com.tmc.attendance.contracts.Direction;
import com.tmc.attendance.contracts.GateResult;
import com.tmc.attendance.contracts.IAttendanceStore;
import com.tmc.attendance.contracts.IGateController;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PendingTransactionRecoveryTest {

    @Test
    void reconcilesPendingTransactionWithConfirmedTurnstileStatus() {
        InMemoryStore store = new InMemoryStore();
        store.saveBeforeOpen(event("tx-1", AttendanceStatus.PENDING));
        IGateController gate = gateWithStatus(GateResult.OPENED);
        PendingTransactionRecovery recovery = new PendingTransactionRecovery(store, gate);

        recovery.reconcilePendingTransactions();

        assertEquals(AttendanceStatus.CONFIRMED, store.events.get("tx-1").status());
    }

    @Test
    void retainsUnresolvedTransactionForAnotherReconciliationAttempt() {
        InMemoryStore store = new InMemoryStore();
        store.saveBeforeOpen(event("tx-2", AttendanceStatus.PENDING_VERIFICATION));
        IGateController gate = gateWithStatus(GateResult.TIMEOUT);
        PendingTransactionRecovery recovery = new PendingTransactionRecovery(store, gate);

        recovery.reconcilePendingTransactions();

        assertEquals(AttendanceStatus.PENDING_VERIFICATION, store.events.get("tx-2").status());
    }

    private static IGateController gateWithStatus(GateResult status) {
        return new IGateController() {
            @Override
            public GateResult open(String transactionId, Direction direction) {
                throw new AssertionError("Recovery must query status, not open the gate");
            }

            @Override
            public GateResult getStatus(String transactionId) {
                return status;
            }
        };
    }

    private static AttendanceEvent event(String transactionId, AttendanceStatus status) {
        return new AttendanceEvent(transactionId, "employee-1", "device-1", "kiosk-1",
                "site-1", Direction.IN, Instant.EPOCH, status, "nonce-1");
    }

    private static final class InMemoryStore implements IAttendanceStore {
        private final Map<String, AttendanceEvent> events = new LinkedHashMap<>();

        @Override
        public void saveBeforeOpen(AttendanceEvent event) {
            events.put(event.transactionId(), event);
        }

        @Override
        public void updateStatus(String transactionId, AttendanceStatus status) {
            events.computeIfPresent(transactionId, (id, event) -> event.withStatus(status));
        }

        @Override
        public List<AttendanceEvent> findByStatus(AttendanceStatus status) {
            return events.values().stream().filter(event -> event.status() == status).toList();
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
