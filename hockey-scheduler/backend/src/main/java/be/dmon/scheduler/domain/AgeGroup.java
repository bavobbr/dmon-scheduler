package be.dmon.scheduler.domain;

public enum AgeGroup {
    U6(6),
    U7(7),
    U8(8),
    U10(10),
    U11(11),
    U12(12),
    U14(14),
    U16(16),
    U19(19),
    SENIOR(99);

    private final int maxAge;

    AgeGroup(int maxAge) {
        this.maxAge = maxAge;
    }

    public int getMaxAge() {
        return maxAge;
    }
}
