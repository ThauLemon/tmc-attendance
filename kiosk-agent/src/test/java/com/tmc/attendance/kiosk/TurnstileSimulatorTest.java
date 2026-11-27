package com.tmc.attendance.kiosk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tmc.attendance.contracts.Direction;
import com.tmc.attendance.contracts.GateResult;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class TurnstileSimulatorTest {

    @Test
    void remembersTransactionResultAndDoesNotReopenSameTransaction() {
        TurnstileSimulator simulator = new TurnstileSimulator(
                new KioskProperties("kiosk-test", "site-test", SimulatorMode.FAULT, Duration.ofSeconds(1)));

        assertEquals(GateResult.FAULT, simulator.open("tx-1", Direction.IN));
        simulator.setMode(SimulatorMode.OK);

        assertEquals(GateResult.FAULT, simulator.open("tx-1", Direction.IN));
        assertEquals(GateResult.FAULT, simulator.getStatus("tx-1"));
    }

    @Test
    void reportsUnknownTransactionsAsUnresolved() {
        TurnstileSimulator simulator = new TurnstileSimulator(
                new KioskProperties("kiosk-test", "site-test", SimulatorMode.OK, Duration.ofSeconds(1)));

        assertEquals(GateResult.TIMEOUT, simulator.getStatus("not-seen"));
    }
}
