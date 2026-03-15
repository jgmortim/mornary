package com.mornary.model;

public enum OperationSize {
    TINY(1, 100),
    SMALL(5, 50),
    MEDIUM(20, 25),
    LARGE(50, 10),
    HUGE(100, 2);

    public final long maxNumberOfWorkUnits;
    public final int matchTarget;

    OperationSize(int maxNumberOfWorkUnits, int matchTarget) {
        this.maxNumberOfWorkUnits = maxNumberOfWorkUnits;
        this.matchTarget = matchTarget;
    }

    public boolean usesReducedDictionarySet() {
        return this.matchTarget >= LARGE.matchTarget;
    }

    public static OperationSize getOperationSize(long numberOfWorkUnits) {
        for (OperationSize size : OperationSize.values()) {
            if (size.maxNumberOfWorkUnits >= numberOfWorkUnits) {
                return size;
            }
        }
        return OperationSize.HUGE;
    }
}
