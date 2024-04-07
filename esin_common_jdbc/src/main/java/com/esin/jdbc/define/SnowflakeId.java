package com.esin.jdbc.define;

import java.util.Calendar;
import java.util.GregorianCalendar;

public final class SnowflakeId {

    private static final long baseTime = new GregorianCalendar(2020, Calendar.JANUARY, 1).getTimeInMillis();

    private long lastTimestamp = System.currentTimeMillis(); // 43 bits, 276年
    private final long workerId; // 8 bits
    private long sequence = 0;  // 12 bits

    public SnowflakeId(long workerId) {
        if (workerId >= 256 || workerId < 0) {
            throw new IllegalArgumentException("workerId is must in [0, 1024).");
        }
        this.workerId = workerId << 12;
    }

    public synchronized long nextId() {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp || timestamp < baseTime) {
            throw new RuntimeException("Clock moved backwards.");
        }
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) % 4096;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }
        lastTimestamp = timestamp;
        return ((timestamp - baseTime) << 20) | workerId | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) {
        SnowflakeId snowflake = new SnowflakeId(1);
        for (int i = 0; i < 3000; i++) {
            System.out.println(snowflake.nextId());
        }
    }

}
