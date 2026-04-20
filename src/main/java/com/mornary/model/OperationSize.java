package com.mornary.model;

public enum OperationSize {
    SMALL(1, 100),
    MEDIUM(5, 50),
    LARGE(20, 25);

    public final long maxNumberOfWorkUnits;
    public final int matchTarget;

    OperationSize(int maxNumberOfWorkUnits, int matchTarget) {
        this.maxNumberOfWorkUnits = maxNumberOfWorkUnits;
        this.matchTarget = matchTarget;
    }

    public static OperationSize getOperationSize(long numberOfWorkUnits) {
        for (OperationSize size : OperationSize.values()) {
            if (size.maxNumberOfWorkUnits >= numberOfWorkUnits) {
                return size;
            }
        }
        return OperationSize.LARGE;
    }
}
