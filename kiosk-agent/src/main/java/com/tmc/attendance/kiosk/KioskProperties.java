package com.tmc.attendance.kiosk;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Cấu hình kiosk, đọc từ khóa {@code kiosk.*} trong application.yml hoặc biến môi trường KIOSK_*. */
@ConfigurationProperties(prefix = "kiosk")
public record KioskProperties(
        @DefaultValue("kiosk-local") String kioskId,
        @DefaultValue("site-01") String siteId,
        @DefaultValue("OK") SimulatorMode simulatorMode,
        @DefaultValue("5s") Duration confirmTimeout) {
}
