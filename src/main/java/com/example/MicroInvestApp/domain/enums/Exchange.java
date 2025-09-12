package com.example.MicroInvestApp.domain.enums;

import java.time.ZoneId;

public enum Exchange {
    NYSE("America/New_York"),
    NASDAQ("America/New_York"),
    TSX("America/Toronto"),
    LSE("Europe/London"),
    HKEX("Asia/Hong_Kong"),
    JPX("Asia/Tokyo"),
    SSE("Asia/Shanghai"),
    SZSE("Asia/Shanghai");

    private final String timezoneId;

    Exchange(String timezoneId) {
        this.timezoneId = timezoneId;
    }

    public ZoneId getTimezoneId() {
        return ZoneId.of(timezoneId);
    }

    public String getTimezoneIdString() {
        return timezoneId;
    }
}